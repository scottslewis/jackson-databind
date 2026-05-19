package tools.jackson.databind;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.std.DelegatingDeserializer;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.TreeTraversingParser;

public class JRefValueDeserializer extends DelegatingDeserializer {

	DeserializationContext ctxt;
	Supplier beanDescRef;
	BeanProperty property;

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("JRefValueDeserializer[");
		buf.append("beanRawClass=").append(beanDescRef.getType().getRawClass());
		buf.append(", property=").append(property).append("]");
		return buf.toString();
	}

	@Override
	public ValueInstantiator getValueInstantiator() {
		return super.getValueInstantiator();
	}
	
	void print(String method, JRefValueDeserializer vds) {
		StringBuffer buf = new StringBuffer(method).append(".");
		buf.append(vds.toString());
		System.out.println(buf.toString());
	}

	void print(String method) {
		print(method, this);
	}

	public JRefValueDeserializer(Supplier beanDescRef, ValueDeserializer<?> src) {
		super(src);
		this.beanDescRef = beanDescRef;
	}

	public JRefValueDeserializer(JRefValueDeserializer src) {
		this(src.beanDescRef, src);
		this.ctxt = src.ctxt;
		this.property = src.property;
	}

	@Override
	public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
		ValueDeserializer<?> result;
		this.ctxt = ctxt;
		this.property = property;
		print("CREATECONTEXTUAL");
		result = super.createContextual(ctxt, property);
		print("CREATEDCONTEXTUAL");
		return result;
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
		print("DESERIALIZE");
		Object result = null;
		JsonToken current = p.currentToken();		
		if (current == JsonToken.START_OBJECT) {
			JsonNode node = this.ctxt.readTree(p);
			if (node instanceof ObjectNode) {
				ObjectNode onode = (ObjectNode) node;
				JsonNode valNode = onode.get("$ref");
				if (valNode != null) {
					String valStr = valNode.asString();
					if (valStr != null) {
						System.out.println("$ref found value=" + valStr);
						@SuppressWarnings("unchecked")
						List<JRefResolver> jrefs = (List<JRefResolver>) ctxt.getAttribute("jrefs");
						if (jrefs == null) {
							jrefs = new ArrayList<JRefResolver>();
						} 
						jrefs.add(new JRefResolver(valStr, this));
						ctxt.setAttribute("jrefs", jrefs);
						return null;
					}
				}
			}
			TreeTraversingParser tpp = new TreeTraversingParser(node);
			tpp.nextToken();
			result = super.deserialize(tpp, ctxt);
		} else {
			result = super.deserialize(p, ctxt);
		}
		print("DESERIALIZED result=" + result);
		return result;
	}

	@Override
	protected ValueDeserializer<?> newDelegatingInstance(ValueDeserializer<?> newDelegatee) {
		return new JRefValueDeserializer(this.beanDescRef,newDelegatee);
	}

}
