package scdf.example.ingestion.tika;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.tuple.JsonStringToTupleConverter;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(properties = {"spring.cloud.stream.bindings.output.contentType=application/json"})
@DirtiesContext
public class TikaExtractionProcessorIT {

	private static final String TMPDIR = System.getProperty("java.io.tmpdir");

	private static final String ROOT_DIR = TMPDIR + File.separator + "dataflow-tests" + File.separator + "input";

    @Test
    public void contextLoads() {
    }

    @Autowired
	private Processor tikaExtractionProcessor;

    @Autowired
    private BinderFactory<MessageChannel> binderFactory;

    @Autowired
    private MessageCollector messageCollector;

    private JsonStringToTupleConverter jsonStringToTupleConverter = new JsonStringToTupleConverter();

    @BeforeClass
    public static void disableTestsOnCiServer() {
        String profilesFromConsole = System.getProperty("spring.profiles.active", "");
        assumeFalse(profilesFromConsole.contains("integration-tests"));
    }

    @Before
    public void setup() {

    }

    @After
    public void teardown() {

    }

    @Test
    @SuppressWarnings("unchecked")
	public void testWiring() throws IOException {

        Map<String, Object> headers = new HashMap<>();

		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("test-documents/testPDF.pdf");

		headers.put("content-type", "text/plain");
		Message<InputStream> message = new GenericMessage<>(inputStream, headers);
		tikaExtractionProcessor.input().send(message);
		Message<String> received = (Message<String>) messageCollector.forChannel(tikaExtractionProcessor.output())
				.poll();
		String doc = received.getPayload();
		assertNotNull(doc);

		System.out.println(doc);
    }

}
