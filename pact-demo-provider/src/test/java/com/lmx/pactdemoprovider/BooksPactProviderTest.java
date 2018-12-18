package com.lmx.pactdemoprovider;

import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.RestPactRunner;
import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit.target.TestTarget;
import au.com.dius.pact.provider.spring.target.MockMvcTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@RunWith(RestPactRunner.class)
@Provider("Some Provider")
@PactFolder("../pact/out")
public class BooksPactProviderTest {

    //Create instance(s) of your controller(s).  We cannot autowire controllers as we're not using (and don't want to use) a Spring test runner.
    @InjectMocks
    private BookController bookController = new BookController();

    //Create the MockMvcTarget with your controller and exception handler.  The third parameter, when set to true, will
    //print verbose request/response information for all interactions with MockMvc.
    @TestTarget
    public final MockMvcTarget target = new MockMvcTarget();

    private final DateTime DATE_TIME = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        target.setControllers(bookController);
        target.setServletPath("/api");
        target.setMessageConvertors(
                new MappingJackson2HttpMessageConverter(
                        new ObjectMapper()
                                .registerModule(new JodaModule())
                                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                )
        );
    }


    @State("update-book")
    public void updateBook() {
        // no setup needed
    }

    @State("delete-book")
    public void deleteBook() {
        // no setup needed
    }

    @State("update-book-no-content-type")
    public void updateBookNoContentType() {
        // no setup needed
    }


}
