/*
  DBFReader
  Class for reading the records assuming that the given
	InputStream comtains DBF data.

  This file is part of JavaDBF packege.

  Author: anil@linuxense.com
  License: LGPL (http://www.gnu.org/copyleft/lesser.html)

  $Id: DBFReader.java,v 1.8 2004/03/31 10:54:03 anil Exp $
*/

package com.linuxense.javadbf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.linuxense.javadbf.DBFDataType.VARBINARY;
import static com.linuxense.javadbf.DBFDataType.VARCHAR;

/**
 * DBFReader class can creates objects to represent DBF data.
 * <p>
 * This Class is used to read data from a DBF file. Meta data and
 * records can be queried against this document.
 *
 * <p>
 * DBFReader cannot write anythng to a DBF file. For creating DBF files
 * use DBFWriter.
 *
 * <p>
 * Fetching rocord is possible only in the forward direction and
 * cannot re-wound. In such situation, a suggested approach is to reconstruct the object.
 * <p>
 * p>
 * he nextRecord() method returns an array of Objects and the types of these
 * bject are as follows:
 *
 * <table>
 * <tr>
 * <th>xBase Type</th><th>Java Type</th>
 * </tr>
 *
 * <tr>
 * <td>C</td><td>String</td>
 * </tr>
 * <tr>
 * <td>N</td><td>Integer</td>
 * </tr>
 * <tr>
 * <td>F</td><td>Double</td>
 * </tr>
 * <tr>
 * <td>L</td><td>Boolean</td>
 * </tr>
 * <tr>
 * <td>D</td><td>java.util.Date</td>
 * </tr>
 * </table>
 */
public class DBFReader extends DBFBase {
    private static final long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final long TIME_MILLIS_1_1_4713_BC = -210866803200000L;
    private DataInputStream dataInputStream;
    private DBFHeader header;
    private Map<String, Integer> mapFieldNames = new HashMap<String, Integer>();
    boolean isClosed = true;
    private DBFMemoFile memoFile = null;
    private boolean closed;
    private boolean showDeletedRows = false;
    private boolean trimRightSpaces = true;

    /**
     * Initializes a DBFReader object.
     * <p>
     * When this constructor returns the object
     * will have completed reading the hader (meta date) and
     * header information can be quried there on. And it will
     * be ready to return the first row.
     */
    public DBFReader(InputStream in) throws DBFException {
        try {
            this.dataInputStream = new DataInputStream(in);
            this.isClosed = false;
            this.header = new DBFHeader();
            this.header.read(this.dataInputStream);
            int t_dataStartIndex = this.header.headerLength - (32 + (32 * this.header.fieldArray.length)) - 1;
            if (t_dataStartIndex > 0) {
                dataInputStream.skip(t_dataStartIndex);
            }
        } catch (IOException e) {
            throw new DBFException(e.getMessage());
        }
    }

    public void close() {
        this.closed = true;
        Utils.close(this.dataInputStream);
    }

    public DBFRow nextRow() throws Exception {
        Object[] record = nextRecord();
        if (record == null) {
            return null;
        }
        return new DBFRow(record, mapFieldNames, this.header.fieldArray);
    }

    public DBFReader(InputStream in, Charset charset) throws DBFException {
        try {
            this.dataInputStream = new DataInputStream(in);
            this.isClosed = false;
            this.header = new DBFHeader();
            this.header.read(this.dataInputStream, charset, false);
            int t_dataStartIndex = this.header.headerLength - (32 + (32 * this.header.fieldArray.length)) - 1;
            if (t_dataStartIndex > 0) {
                dataInputStream.skip(t_dataStartIndex);
            }
            this.mapFieldNames = createMapFieldNames(this.header.userFieldArray);
        } catch (IOException e) {
            throw new DBFException(e.getMessage());
        }
    }

    private Map<String, Integer> createMapFieldNames(DBFField[] fieldArray) {
        Map<String, Integer> fieldNames = new HashMap<String, Integer>();
        for (int i = 0; i < fieldArray.length; i++) {
            String name = fieldArray[i].getName();
            fieldNames.put(name.toLowerCase(), i);
        }
        return Collections.unmodifiableMap(fieldNames);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(this.header.year + "/" + this.header.month + "/" + this.header.day + "\n"
                + "Total records: " + this.header.numberOfRecords +
                "\nHEader length: " + this.header.headerLength +
                "");
        for (int i = 0; i < this.header.fieldArray.length; i++) {
            sb.append(this.header.fieldArray[i].getName());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns the number of records in the DBF.
     */
    public int getRecordCount() {
        return this.header.numberOfRecords;
    }

    /**
     * Returns the asked Field. In case of an invalid index,
     * it returns a ArrayIndexOutofboundsException.
     */
    public DBFField getField(int index) throws DBFException {
        if (isClosed) {
            throw new DBFException("Source is not open");
        }
        return this.header.fieldArray[index];
    }

    /**
     * Returns the number of field in the DBF.
     */
    public int getFieldCount() throws DBFException {
        if (isClosed) {
            throw new DBFException("Source is not open");
        }
        if (this.header.fieldArray != null) {
            return this.header.fieldArray.length;
        }
        return -1;
    }

    /**
     * Reads the returns the next row in the DBF stream.
     *
     * @returns The next row as an Object array. Types of the elements
     * these arrays follow the convention mentioned in the class description.
     */
    public Object[] nextRecord() throws Exception {
        if (this.closed) {
            throw new IllegalArgumentException("this DBFReader is closed");
        }
        List<Object> recordObjects = new ArrayList<>(this.getFieldCount());
        try {
            boolean isDeleted = false;
            do {
                try {
                    if (isDeleted && !showDeletedRows) {
                        skip(this.header.recordLength - 1);
                    }
                    int t_byte = this.dataInputStream.readByte();
                    if (t_byte == END_OF_DATA || t_byte == -1) {
                        return null;
                    }
                    isDeleted = t_byte == '*';
                } catch (EOFException e) {
                    return null;
                }
            } while (isDeleted && !showDeletedRows);
            if (showDeletedRows) {
                recordObjects.add(isDeleted);
            }
            for (int i = 0; i < this.header.fieldArray.length; i++) {
                DBFField field = this.header.fieldArray[i];
                Object o = getFieldValue(field);
                if (field.isSystem()) {
                    if (field.getType() == DBFDataType.NULL_FLAGS && o instanceof BitSet) {
                        BitSet nullFlags = (BitSet) o;
                        int currentIndex = -1;
                        for (int j = 0; j < this.header.fieldArray.length; j++) {
                            DBFField field1 = this.header.fieldArray[j];
                            if (field1.isNullable()) {
                                currentIndex++;
                                if (nullFlags.get(currentIndex)) {
                                    recordObjects.set(j, null);
                                }
                            }
                            if (field1.getType() == VARBINARY || field1.getType() == VARCHAR) {
                                currentIndex++;
                                if (recordObjects.get(i) instanceof byte[]) {
                                    byte[] data = (byte[]) recordObjects.get(j);
                                    int size = field1.getLength();
                                    if (!nullFlags.get(currentIndex)) {
                                        // Data is not full
                                        size = data[data.length - 1];
                                    }
                                    byte[] newData = new byte[size];
                                    System.arraycopy(data, 0, newData, 0, size);
                                    Object o1 = newData;
                                    if (field1.getType() == VARCHAR) {
                                        o1 = new String(newData, getCharset());
                                    }
                                    recordObjects.set(j, o1);
                                }
                            }
                        }
                    }
                } else {
                    recordObjects.add(o);
                }
            }
        } catch (EOFException e) {
            throw new Exception(e.getMessage(), e);
        } catch (IOException e) {
            throw new Exception(e.getMessage(), e);
        }
        return recordObjects.toArray();
    }

    protected void skip(int bytesToSkip) throws IOException {
        int skipped = (int) this.dataInputStream.skip(bytesToSkip);
        for (int i = skipped; i < bytesToSkip; i++) {
            this.dataInputStream.readByte();
        }
    }

    protected Object getFieldValue(DBFField field) throws IOException {
        int bytesReaded = 0;
        switch (field.getType()) {
            case CHARACTER:
                byte b_array[] = new byte[field.getLength()];
                bytesReaded = this.dataInputStream.read(b_array);
                if (bytesReaded < field.getLength()) {
                    throw new EOFException("Unexpected end of file");
                }
                if (this.trimRightSpaces) {
                    return new String(DBFUtils.trimRightSpaces(b_array), getCharset());
                } else {
                    return new String(b_array, getCharset());
                }
            case VARCHAR:
            case VARBINARY:
                byte b_array_var[] = new byte[field.getLength()];
                bytesReaded = this.dataInputStream.read(b_array_var);
                if (bytesReaded < field.getLength()) {
                    throw new EOFException("Unexpected end of file");
                }
                return b_array_var;
            case DATE:
                byte t_byte_year[] = new byte[4];
                bytesReaded = this.dataInputStream.read(t_byte_year);
                if (bytesReaded < 4) {
                    throw new EOFException("Unexpected end of file");
                }
                byte t_byte_month[] = new byte[2];
                bytesReaded = this.dataInputStream.read(t_byte_month);
                if (bytesReaded < 2) {
                    throw new EOFException("Unexpected end of file");
                }
                byte t_byte_day[] = new byte[2];
                bytesReaded = this.dataInputStream.read(t_byte_day);
                if (bytesReaded < 2) {
                    throw new EOFException("Unexpected end of file");
                }
                try {
                    GregorianCalendar calendar = new GregorianCalendar(Integer.parseInt(new String(t_byte_year, StandardCharsets.US_ASCII)),
                            Integer.parseInt(new String(t_byte_month, StandardCharsets.US_ASCII)) - 1,
                            Integer.parseInt(new String(t_byte_day, StandardCharsets.US_ASCII)));
                    return calendar.getTime();
                } catch (NumberFormatException e) {
                    // this field may be empty or may have improper value set
                    return null;
                }
            case FLOATING_POINT:
            case NUMERIC:
                return DBFUtils.readNumericStoredAsText(this.dataInputStream, field.getLength());
            case LOGICAL:
                byte t_logical = this.dataInputStream.readByte();
                return DBFUtils.toBoolean(t_logical);
            case LONG:
            case AUTOINCREMENT:
                int data = DBFUtils.readLittleEndianInt(this.dataInputStream);
                return data;
            case CURRENCY:
                int c_data = DBFUtils.readLittleEndianInt(this.dataInputStream);
                String s_data = String.format("%05d", c_data);
                String x1 = s_data.substring(0, s_data.length() - 4);
                String x2 = s_data.substring(s_data.length() - 4);
                skip(field.getLength() - 4);
                return new BigDecimal(x1 + "." + x2);
            case TIMESTAMP:
            case TIMESTAMP_DBASE7:
                int days = DBFUtils.readLittleEndianInt(this.dataInputStream);
                int time = DBFUtils.readLittleEndianInt(this.dataInputStream);
                if (days == 0 && time == 0) {
                    return null;
                } else {
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTimeInMillis(days * MILLISECS_PER_DAY + TIME_MILLIS_1_1_4713_BC + time);
                    calendar.add(Calendar.MILLISECOND, -TimeZone.getDefault().getOffset(calendar.getTimeInMillis()));
                    return calendar.getTime();
                }
            case MEMO:
            case GENERAL_OLE:
            case PICTURE:
            case BLOB:
                return readMemoField(field);
            case BINARY:
                if (field.getLength() == 8) {
                    return readDoubleField(field);
                } else {
                    return readMemoField(field);
                }
            case DOUBLE:
                return readDoubleField(field);
            case NULL_FLAGS:
                byte[] data1 = new byte[field.getLength()];
                int readed = dataInputStream.read(data1);
                if (readed != field.getLength()) {
                    throw new EOFException("Unexpected end of file");
                }
                return BitSet.valueOf(data1);
            default:
                skip(field.getLength());
                return null;
        }
    }

    private Object readMemoField(DBFField field) throws IOException {
        Number nBlock = null;
        if (field.getLength() == 10) {
            nBlock = DBFUtils.readNumericStoredAsText(this.dataInputStream, field.getLength());
        } else {
            nBlock = DBFUtils.readLittleEndianInt(this.dataInputStream);
        }
        if (this.memoFile != null && nBlock != null) {
            return memoFile.readData(nBlock.intValue(), field.getType());
        }
        return null;
    }

    private Object readDoubleField(DBFField field) throws IOException {
        byte[] data = new byte[field.getLength()];
        int bytesReaded = this.dataInputStream.read(data);
        if (bytesReaded < field.getLength()) {
            throw new EOFException("Unexpected end of file");
        }
        return ByteBuffer.wrap(
                new byte[]{
                        data[7], data[6], data[5], data[4],
                        data[3], data[2], data[1], data[0]
                }).getDouble();
    }

}
