/*
  DBFField
	Class represents a "field" (or column) definition of a DBF data structure.

  This file is part of JavaDBF packege.

  author: anil@linuxense.com
  license: LGPL (http://www.gnu.org/copyleft/lesser.html)

  $Id: DBFField.java,v 1.7 2004/03/31 10:50:11 anil Exp $
*/

package com.linuxense.javadbf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * DBFField represents a field specification in an dbf file.
 * <p>
 * DBFField objects are either created and added to a DBFWriter object or obtained
 * from DBFReader object through getField( int) query.
 */
public class DBFField {

    public static final byte FIELD_TYPE_C = (byte) 'C';
    public static final byte FIELD_TYPE_L = (byte) 'L';
    public static final byte FIELD_TYPE_N = (byte) 'N';
    public static final byte FIELD_TYPE_F = (byte) 'F';
    public static final byte FIELD_TYPE_D = (byte) 'D';
    public static final byte FIELD_TYPE_M = (byte) 'M';
    private String name;
    private DBFDataType type; /* 11 */
    private int length; /* 16 */
    /* Field struct variables start here */
    byte[] fieldName = new byte[11]; /* 0-10*/
    byte dataType;                    /* 11 */
    int reserv1;                      /* 12-15 */
    int fieldLength;                 /* 16 */
    byte decimalCount;                /* 17 */
    short reserv2;                    /* 18-19 */
    byte workAreaId;                  /* 20 */
    short reserv3;                    /* 21-22 */
    byte setFieldsFlag;               /* 23 */
    byte[] reserv4 = new byte[7];    /* 24-30 */
    byte indexFieldFlag;              /* 31 */
    /* Field struct variables end here */

    /* other class variables */
    int nameNullIndex = 0;

    /**
     * Creates a DBFField object from the data read from the given DataInputStream.
     * <p>
     * The data in the DataInputStream object is supposed to be organised correctly
     * and the stream "pointer" is supposed to be positioned properly.
     *
     * @param in DataInputStream
     * @return Returns the created DBFField object.
     * @throws IOException If any stream reading problems occures.
     */
    protected static DBFField createField(DataInput in, Charset charset, boolean useFieldFlags) throws IOException {

        DBFField field = new DBFField();
        byte t_byte = in.readByte(); /* 0 */
        if (t_byte == (byte) 0x0d) {
            return null;
        }
        byte[] fieldName = new byte[11];
        in.readFully(fieldName, 1, 10); /* 1-10 */
        fieldName[0] = t_byte;
        int nameNullIndex = fieldName.length - 1;
        for (int i = 0; i < fieldName.length; i++) {
            if (fieldName[i] == (byte) 0) {
                nameNullIndex = i;
                break;
            }
        }
        field.name = new String(fieldName, 0, nameNullIndex, charset);
        try {
            field.type = DBFDataType.fromCode(in.readByte()); /* 11 */
        } catch (Exception e) {
            field.type = DBFDataType.UNKNOWN;
        }
        field.reserv1 = Utils.readLittleEndianInt(in); /* 12-15 */
        field.length = in.readUnsignedByte(); /* 16 */
        field.decimalCount = in.readByte(); /* 17 */
        field.reserv2 = Utils.readLittleEndianShort(in); /* 18-19 */
        field.workAreaId = in.readByte(); /* 20 */
        field.reserv3 = Utils.readLittleEndianShort(in); /* 21-22 */
        field.setFieldsFlag = in.readByte(); /* 23 */
        in.readFully(field.reserv4); /* 24-30 */
        field.indexFieldFlag = in.readByte(); /* 31 */
        adjustLengthForLongCharSupport(field);

        if (!useFieldFlags) {
            field.reserv2 = 0;
        }

        return field;
    }

    private static void adjustLengthForLongCharSupport(DBFField field) {
        // if field type is char or varchar, then read length and decimalCount as one number to allow char fields to be
        // longer than 256 bytes.
        // This is the way Clipper and FoxPro do it, and there is really no downside
        // since for char fields decimal count should be zero for other versions that do not support this extended functionality.
        if (field.type == DBFDataType.CHARACTER || field.type == DBFDataType.VARCHAR) {
            field.length |= field.decimalCount << 8;
            field.decimalCount = 0;
        }
    }

    protected static DBFField createField(DataInput in) throws IOException {
        DBFField field = new DBFField();
        byte t_byte = in.readByte(); /* 0 */
        if (t_byte == (byte) 0x0d) {
            return null;
        }
        in.readFully(field.fieldName, 1, 10);    /* 1-10 */
        field.fieldName[0] = t_byte;
        for (int i = 0; i < field.fieldName.length; i++) {
            if (field.fieldName[i] == (byte) 0) {
                field.nameNullIndex = i;
                break;
            }
        }
        field.name = new String(field.fieldName, "GBK");
        field.dataType = in.readByte(); /* 11 */
        field.reserv1 = Utils.readLittleEndianInt(in); /* 12-15 */
        field.fieldLength = in.readUnsignedByte();  /* 16 */
        field.decimalCount = in.readByte(); /* 17 */
        field.reserv2 = Utils.readLittleEndianShort(in); /* 18-19 */
        field.workAreaId = in.readByte(); /* 20 */
        field.reserv2 = Utils.readLittleEndianShort(in); /* 21-22 */
        field.setFieldsFlag = in.readByte(); /* 23 */
        in.readFully(field.reserv4); /* 24-30 */
        field.indexFieldFlag = in.readByte(); /* 31 */
        return field;
    }

    /**
     * Writes the content of DBFField object into the stream as per
     * DBF format specifications.
     *
     * @throws IOException if any stream related issues occur.
     */
    protected void write(DataOutput out) throws IOException {
        //DataOutputStream out = new DataOutputStream( os);
        // Field Name
        out.write(fieldName);        /* 0-10 */
        out.write(new byte[11 - fieldName.length]);
        // data type
        out.writeByte(dataType); /* 11 */
        out.writeInt(0x00);   /* 12-15 */
        out.writeByte(fieldLength); /* 16 */
        out.writeByte(decimalCount); /* 17 */
        out.writeShort((short) 0x00); /* 18-19 */
        out.writeByte((byte) 0x00); /* 20 */
        out.writeShort((short) 0x00); /* 21-22 */
        out.writeByte((byte) 0x00); /* 23 */
        out.write(new byte[7]); /* 24-30*/
        out.writeByte((byte) 0x00); /* 31 */
    }

    /**
     Returns the name of the field.

     @return Name of the field as String.
     */
    /**
     * Returns the name of the field.
     *
     * @return Name of the field as String.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the data type of the field.
     *
     * @return Data type as byte.
     */
    public byte getDataType() {
        return dataType;
    }

    public boolean isSystem() {
        return (this.reserv2 & 1) != 0;
    }

    /**
     * Returns field length.
     *
     * @return field length as int.
     */
    public int getFieldLength() {
        return fieldLength;
    }

    /**
     * Returns the decimal part. This is applicable
     * only if the field type if of numeric in nature.
     * <p>
     * If the field is specified to hold integral values
     * the value returned by this method will be zero.
     *
     * @return decimal field size as int.
     */
    public int getDecimalCount() {
        return decimalCount;
    }

    /**
     * @deprecated This method is depricated as of version 0.3.3.1 and is replaced by {@link #setName(String)}.
     */
    public void setFieldName(String value) {
        setName(value);
    }

    /**
     * Sets the name of the field.
     *
     * @since 0.3.3.1
     */
    public void setName(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Field name cannot be null");
        }
        if (value.length() == 0 || value.length() > 10) {
            throw new IllegalArgumentException("Field name should be of length 0-10");
        }
        this.fieldName = value.getBytes();
        this.nameNullIndex = this.fieldName.length;
    }

    /**
     * Sets the data type of the field.
     * <p>
     * C, L, N, F, D, M
     */
    public void setDataType(byte value) {
        switch (value) {
            case 'D':
                this.fieldLength = 8; /* fall through */
            case 'C':
            case 'L':
            case 'N':
            case 'F':
            case 'M':
                this.dataType = value;
                break;
            default:
                throw new IllegalArgumentException("Unknown data type");
        }
    }

    /**
     * Length of the field.
     * This method should be called before calling setDecimalCount().
     */
    public void setFieldLength(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Field length should be a positive number");
        }
        if (this.dataType == FIELD_TYPE_D) {
            throw new UnsupportedOperationException("Cannot do this on a Date field");
        }
        fieldLength = value;
    }

    /**
     * Sets the decimal place size of the field.
     * Before calling this method the size of the field
     * should be set by calling setFieldLength().
     */
    public void setDecimalCount(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Decimal length should be a positive number");
        }
        if (value > fieldLength) {
            throw new IllegalArgumentException("Decimal length should be less than field length");
        }
        decimalCount = (byte) value;
    }

    public DBFDataType getType() {
        return this.type;
    }

    /**
     * Checks if the field is nullable
     *
     * @return true if the field is nullable
     */
    public boolean isNullable() {
        return (this.reserv2 & 2) != 0;
    }

    /**
     * Checks if the field is binary
     *
     * @return true if the field is binary
     */
    public boolean isBinary() {
        return (this.reserv2 & 4) != 0;
    }

    public int getLength() {
        return this.length;
    }

}
