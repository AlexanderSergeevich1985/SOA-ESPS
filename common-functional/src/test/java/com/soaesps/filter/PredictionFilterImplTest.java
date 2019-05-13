package com.soaesps.filter;

import com.soaesps.core.BaseOperation.Filter.PredictionFilter;
import com.soaesps.core.BaseOperation.Filter.PredictionFilterImpl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthApplication.class)
@WebAppConfiguration
public class PredictionFilterImplTest {
    @TestConfiguration
    static class EmployeeServiceImplTestContextConfiguration {

        @Bean
        public PredictionFilter predictionFilter() {
            return new PredictionFilterImpl();
        }
    }

    @Autowired
    private PredictionFilter predictionFilter;

    @Before
    public void setup() {
        initMocks(this);
    }
}