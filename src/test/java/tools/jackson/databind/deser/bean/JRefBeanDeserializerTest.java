package tools.jackson.databind.deser.bean;

import static tools.jackson.databind.testutil.DatabindTestUtil.jsonMapperBuilder;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.JavaType;
@SuppressWarnings("serial")
public class JRefBeanDeserializerTest
{
    static class ModuleImpl extends SimpleModule
    {
        protected ValueDeserializerModifier modifier;

        public ModuleImpl(ValueDeserializerModifier modifier)
        {
            super("test", Version.unknownVersion());
            this.modifier = modifier;
        }

        @Override
        public void setupModule(SetupContext context)
        {
            super.setupModule(context);
            if (modifier != null) {
                context.addDeserializerModifier(modifier);
            }
        }
    }

    static class JRefBeanDeserializer extends StdDeserializer<Object> {
    	
    	StdDeserializer<Object> delegate;    	
    	DeserializationContext context;
    	BeanProperty property;
    	
		public JRefBeanDeserializer(StdDeserializer<Object> src) {
    		super(src);
    		this.delegate = src;
    	}
    	
		@Override
        public ValueDeserializer<?> createContextual(DeserializationContext ctxt,
                BeanProperty property)
        {
        	JavaType valueType = getValueType();
        	System.out.println("valueType=" + valueType);
        	JavaType contextType = ctxt.getContextualType();
        	System.out.println("contextType=" + contextType);
        	JavaType propertyType = (property != null) ? property.getType() : null;
        	System.out.println("propertyType=" + propertyType);
			// Set context for this deserializer
			this.context = ctxt;
			this.property = property;
        	delegate.createContextual(ctxt, property);
        	return this;
        }

		@Override
		public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
			System.out.println("deserialize prop=" + this.property);
			Object result = this.delegate.deserialize(p, ctxt);
			System.out.println("deserializer result=" + result);
			return result;
		}
    }
    
    static class RemovingModifier extends ValueDeserializerModifier
    {
		@SuppressWarnings("unchecked")
		@Override
        public ValueDeserializer<?> modifyDeserializer(DeserializationConfig config, Supplier beanDescRef,
        		ValueDeserializer<?> deserializer) {
        	if (deserializer instanceof StdDeserializer) {
        		return new JRefBeanDeserializer((StdDeserializer<Object>) deserializer);
        	}
        	return deserializer;
        }
    }

	// types with (self) references
	static class Human {
		@JsonProperty
		String name;
		@JsonProperty
		Human parent;
		@JsonProperty
		Map<String, Object> props;

		public Human() {}
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


    /*
    /********************************************************
    /* Test methods
    /********************************************************
     */

    @Test
    public void testPropertyRemoval() throws Exception
    {
        ObjectMapper mapper = jsonMapperBuilder()
                .addModule(new ModuleImpl(new RemovingModifier()))
                .build();
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
    	
        
        Message msg = mapper.readValue(serMessage, Message.class);
        System.out.println(msg);
    }


}
