package com.soaesps.Utils.sql;

import com.soaesps.core.Utils.sql.SQLHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SQLHelper.class})
public class SQLHelperTest {
    private SQLHelper helper;

    @Test
    public void sqlComposeCorrect() {
        System.out.println(getWhatSelect("id", "start_time", "end_time"));
        System.out.println(SQLHelper.composeSelect(getWhatSelect("id", "start_time", "end_time"), "jobb_desc", "1=1"));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", 1l);
        values.put("start_time", LocalDateTime.now());
        Double d = 1.0d;
        values.put("end_time", d);
        String t = "hhbhb";
        values.put("test", t);
        System.out.println(SQLHelper.getWhatUpdate(values));
        System.out.println(SQLHelper.getWhatInsert(values));
        System.out.println(SQLHelper.getValuesInsert(values));
        System.out.println(SQLHelper.composeMerge("jobb_desc", "1=1", SQLHelper.getWhatUpdate(values), SQLHelper.getWhatInsert(values), SQLHelper.getValuesInsert(values)));
    }

    protected String getWhatSelect(final String... fields) {
        return String.join(",", fields);
    }
}