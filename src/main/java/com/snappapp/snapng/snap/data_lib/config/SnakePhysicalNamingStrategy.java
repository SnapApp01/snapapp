package com.snappapp.snapng.snap.data_lib.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

public class SnakePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {
    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment context) {
        return new Identifier(addUnderscores(logicalName.getText()), logicalName.isQuoted());
    }

    protected static String addUnderscores(String name) {
        StringBuilder buf = new StringBuilder(name.replace('.', '_'));

        for(int i = 1; i < buf.length() - 1; ++i) {
            if (Character.isLowerCase(buf.charAt(i - 1)) && Character.isUpperCase(buf.charAt(i)) && Character.isLowerCase(buf.charAt(i + 1))) {
                buf.insert(i++, '_');
            }
        }

        return buf.toString().toLowerCase(Locale.ROOT);
    }
}
