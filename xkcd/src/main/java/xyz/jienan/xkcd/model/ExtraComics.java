package xyz.jienan.xkcd.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

@Entity
public class ExtraComics {

    @Id(assignable = true)
    public long num;

    public String title;

    public String date;

    public String img;

    public String explain;

    @Convert(converter = ListConverter.class, dbType = String.class)
    public List<String> links;

    public static class ListConverter implements PropertyConverter<List<String>, String> {


        @Override
        public List<String> convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(databaseValue.split("\\|\\|"));
        }

        @Override
        public String convertToDatabaseValue(List<String> entityProperty) {
            return join(entityProperty, 0, "||");
        }
    }

    private static String join(List<String> strings, int startIndex, String separator) {
        final StringBuilder sb = new StringBuilder();
        for (int i=startIndex; i < strings.size(); i++) {
            if (i != startIndex) sb.append(separator);
            sb.append(strings.get(i));
        }
        return sb.toString();
    }
}
