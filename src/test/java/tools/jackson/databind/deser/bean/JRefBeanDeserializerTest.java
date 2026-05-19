package tools.jackson.databind.deser.bean;

import static tools.jackson.databind.testutil.DatabindTestUtil.jsonMapperBuilder;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.core.Version;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JRefValueDeserializer;
import tools.jackson.databind.JRefValueInstantiator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.ValueInstantiators;
import tools.jackson.databind.module.SimpleModule;

@SuppressWarnings("serial")
public class JRefBeanDeserializerTest {
	static class JRefModule extends SimpleModule {
		public JRefModule() {
			super("JRef", Version.unknownVersion());
		}

		@Override
		public void setupModule(SetupContext context) {
			super.setupModule(context);
			context.addDeserializerModifier(new ValueDeserializerModifier() {
				@Override
				public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config, Supplier beanDescRef,
						ValueDeserializer<?> deserializer) {
					return new JRefValueDeserializer(beanDescRef, deserializer);
				}
			});
			context.addValueInstantiators(new ValueInstantiators.Base() {

				@Override
				public ValueInstantiator modifyValueInstantiator(DeserializationConfig config,
						BeanDescription.Supplier beanDescRef, ValueInstantiator defaultInstantiator) {
					return new JRefValueInstantiator(beanDescRef, defaultInstantiator);
				}
			});
		}
	}

	static class Human {
		@JsonProperty
		String name;
		@JsonProperty
		Human parent;
		@JsonProperty
		Map<String, Object> props;
		@JsonProperty
		Human o;

		public Human() {
		}

		@Override
		public String toString() {
			return "Human[name=" + name + ", parent=" + parent + ", props=" + props + ", o=" + this.o + "]";
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

	@Test
	public void testJRef() throws Exception {
		ObjectMapper mapper = jsonMapperBuilder().addModule(new JRefModule())
				.build();

		String serMessage = "{\r\n" + "  \"items\" : [ {\r\n" + "    \"name\" : \"wendy\",\r\n"
				+ "    \"parent\" : {\r\n" + "      \"name\" : \"sam\",\r\n" + "      \"parent\" : null,\r\n"
				+ "      \"props\" : {\r\n" + "        \"s1\" : 1\r\n" + "      }\r\n" + "    },\r\n"
				+ "    \"props\" : {\r\n" + "      \"q\" : \"r\"\r\n" + "    },\r\n"
				+ "    \"o\": { \"$ref\": \"#/items/0/parent\" }\r\n" + "  }, {\r\n" + "    \"name\" : \"rick\",\r\n"
				+ "    \"parent\" : {\r\n" + "      \"$ref\" : \"#/items/0/parent\"\r\n" + "    }\r\n" + "  } ]\r\n"
				+ "}";

		Message msg = mapper.readValue(serMessage, Message.class);
		System.out.println(msg);
	}

}
