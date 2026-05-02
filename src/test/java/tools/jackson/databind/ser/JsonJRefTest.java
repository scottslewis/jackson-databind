package tools.jackson.databind.ser;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JRef;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.testutil.DatabindTestUtil;

public class JsonJRefTest extends DatabindTestUtil {

	// types with (self) references
	static class Human {
		@JsonProperty
		String name;
		@JsonProperty
		Human parent;
		@JsonProperty
		Map<String, Object> props;

		public Human(String name, Human parent, Map<String, Object> d) {
			this.name = name;
			this.parent = parent;
			this.props = d;
		}

		@Override
		public String toString() {
			return "Human[name=" + name + ", parent=" + parent + ", props=" + props + "]";
		}

	}

	static class Message {
		@JsonProperty
		List<Human> items;

		public Message(List<Human> items) {
			this.items = items;
		}

		@Override
		public String toString() {
			return "Message[items=" + items + "]";
		}
	}

	final ObjectMapper MAPPER = jsonMapperBuilder()
		    .enable(SerializationFeature.INDENT_OUTPUT)
		    // to allow serialization of "empty" POJOs (no properties to serialize)
		    // (without this setting, an exception is thrown in those cases)
		    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		    // DeserializationFeature for changing how JSON is read as POJOs:
		    // to prevent exception when encountering unknown property:
		    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		    // to allow coercion of JSON empty String ("") to null Object value:
		    .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
		    .build();
	
	final ObjectMapper JREFMAPPER = jsonMapperBuilder()
		    .enable(SerializationFeature.INDENT_OUTPUT)
		    // to allow serialization of "empty" POJOs (no properties to serialize)
		    // (without this setting, an exception is thrown in those cases)
		    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		    // DeserializationFeature for changing how JSON is read as POJOs:
		    // to prevent exception when encountering unknown property:
		    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		    // to allow coercion of JSON empty String ("") to null Object value:
		    .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
		    .enable(MapperFeature.USE_JREF)
		    .build();

	final ObjectMapper JREFMAPPER1 = jsonMapperBuilder()
		    .enable(SerializationFeature.INDENT_OUTPUT)
		    // to allow serialization of "empty" POJOs (no properties to serialize)
		    // (without this setting, an exception is thrown in those cases)
		    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		    // DeserializationFeature for changing how JSON is read as POJOs:
		    // to prevent exception when encountering unknown property:
		    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		    // to allow coercion of JSON empty String ("") to null Object value:
		    .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
		    .enable(MapperFeature.USE_JREF)
		    .build();

	final JRef JREF = new JRef();
	
    protected Message buildMessage() {
    	// Construct a human tree
    	// sam is root...null/parent
    	var sam = new Human("sam", null, Map.of("s1",1));
    	// wendy is child of sam
    	// Also put ref to sam in properties 'p' along with other props
    	var wmap = Map.of("p", sam, "q","r");
    	var wendy = new Human("wendy", sam, wmap);
    	// rick is also child of sam, with reference to same props as wendy
    	var rick = new Human("rick", sam, wmap);
    	// put wendy and rick into message as list of humans
    	return new Message(List.of(wendy,rick));
    }
    
	@Test 
	void testBuildAndResolveRefs() throws Exception {
		System.out.println("---testBuildAndResolveRefs");
		var message = buildMessage();
		System.out.println("message="+message);
		var buildRefResult = JREF.buildRefs(message);
		System.out.println("jrefSerResult="+buildRefResult);
		var resolveRefResult = JREF.resolveRefs(buildRefResult);
		System.out.println("resolveRefResult="+resolveRefResult);
	}
	
	@Test 
	void testObjectMapperJRef() throws Exception {
		System.out.println("---testObjectMapperJRef");
		var message = buildMessage();
		System.out.println("message="+message);
		var serializedMessage = JREFMAPPER.writeValueAsString(message);
		System.out.println("serializedMessage="+serializedMessage);
		Message deserializedMessage = JREFMAPPER1.readValue(serializedMessage, Message.class);
		System.out.println("deserializedMessage=" + deserializedMessage);
	}
	
	@Test 
	void testDeserializationOnly() throws Exception {
	String serMessage = "{\r\n"
			+ "  \"items\" : [ {\r\n"
			+ "    \"name\" : \"wendy\",\r\n"
			+ "    \"parent\" : {\r\n"
			+ "      \"name\" : \"sam\",\r\n"
			+ "      \"parent\" : null,\r\n"
			+ "      \"props\" : {\r\n"
			+ "        \"s1\" : 1\r\n"
			+ "      }\r\n"
			+ "    },\r\n"
			+ "    \"props\" : {\r\n"
			+ "      \"p\" : {\r\n"
			+ "        \"$ref\" : \"#/items/0/parent\"\r\n"
			+ "      },\r\n"
			+ "      \"q\" : \"r\"\r\n"
			+ "    }\r\n"
			+ "  }, {\r\n"
			+ "    \"name\" : \"rick\",\r\n"
			+ "    \"parent\" : {\r\n"
			+ "      \"$ref\" : \"#/items/0/parent\"\r\n"
			+ "    },\r\n"
			+ "    \"props\" : {\r\n"
			+ "      \"$ref\" : \"#/items/0/props\"\r\n"
			+ "    }\r\n"
			+ "  } ]\r\n"
			+ "}";
	
			// Use MAPPER / no JRef
			Object value = MAPPER.readValue(serMessage, Message.class);
			System.out.println(value);
	}
}
