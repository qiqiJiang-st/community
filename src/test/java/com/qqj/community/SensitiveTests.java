package com.qqj.community;

import com.qqj.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    void testSensitiveFilter(){
        String text="我吸毒，我抽烟，我酗酒，我f☆a☆b☆c☆d☆，但我是个好青年";
        text = sensitiveFilter.filter(text);
        System.out.println(text);


    }
}
