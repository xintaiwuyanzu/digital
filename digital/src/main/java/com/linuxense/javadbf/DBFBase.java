package com.linuxense.javadbf;

import java.nio.charset.Charset;

/**
 * Base class for DBFReader and DBFWriter. Support for choosing implemented
 * character Sets as suggested by Nick Voznesensky darkers@mail.ru
 */
public abstract class DBFBase {
    protected static final int END_OF_DATA = 0x1A;
    protected static final Charset DEFAULT_CHARSET = Charset.forName("GBK");
    private Charset charset = DEFAULT_CHARSET;

    protected DBFBase() {
        super();
    }

    /**
     * Gets the charset used to read and write files.
     *
     * @return charset used to read and write files
     */
    public Charset getCharset() {
        return this.charset;
    }

    /**
     * Sets the charset to use to read and write files.
     * <p>
     * If the library is used in a non-latin environment use this method to set
     * corresponding character set. More information:
     * http://www.iana.org/assignments/character-sets Also see the documentation
     * of the class java.nio.charset.Charset
     *
     * @param charset charset to use
     * @deprecated set the charset in DBFWriter or DBFReader constructors
     */
    // TODO set this metdhod protected in 2.0
    @Deprecated
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Gets the charset used to read and write files.
     *
     * @return name of the charset
     * @deprecated replaced by {@link DBFBase#getCharset()}
     */
    @Deprecated
    public String getCharactersetName() {
        return this.charset.displayName();
    }

    /**
     * Sets the charset to use to read and write files.
     * <p>
     * If the library is used in a non-latin environment use this method to set
     * corresponding character set. More information:
     * http://www.iana.org/assignments/character-sets Also see the documentation
     * of the class java.nio.charset.Charset
     *
     * @param characterSetName name of the charset
     * @deprecated replaced by {@link DBFBase#setCharset(Charset)}
     */
    @Deprecated
    public void setCharactersetName(String characterSetName) {
        this.charset = Charset.forName(characterSetName);
    }

}
