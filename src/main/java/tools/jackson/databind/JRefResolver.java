package tools.jackson.databind;

import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.ValueInstantiator;

public class JRefResolver {

	private final String path;
	private final JRefValueDeserializer deserializer;
	
	public JRefResolver(String path, JRefValueDeserializer deserializer) {
		this.path = path;
		this.deserializer = deserializer;
	}
	
	public Object resolve(Object root) {
		// XXX this always seems to return null;
		ValueInstantiator i = this.deserializer.getValueInstantiator();
		if (i != null) {
			SettableBeanProperty[] props = i.getFromObjectArguments(this.deserializer.ctxt.getConfig());
		
		}
		return null;
	}
}
