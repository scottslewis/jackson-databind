package tools.jackson.databind;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.ValueInstantiator;
import tools.jackson.databind.deser.bean.PropertyValueBuffer;

public class JRefValueInstantiator extends ValueInstantiator.Delegating {

	private static final long serialVersionUID = 1L;
	DeserializationContext ctxt;
	Supplier beanDescRef;

	// Called by ValueInstantiatorModifier...no context
	public JRefValueInstantiator(Supplier beanDescRef, ValueInstantiator defaultInstantiator) {
		super(defaultInstantiator);
		this.beanDescRef = beanDescRef;
	}

	@Override
	public ValueInstantiator createContextual(DeserializationContext ctxt, Supplier beanDescRef) {
		this.ctxt = ctxt;
		return super.createContextual(ctxt, beanDescRef);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("JRefValueInstantiator[");
		buf.append("beanRawClass=").append(beanDescRef.getType().getRawClass()).append("]");
		return buf.toString();
	}

	@Override
	public SettableBeanProperty[] getFromObjectArguments(DeserializationConfig config) {
		SettableBeanProperty[] settableBeanProperty = super.getFromObjectArguments(config);
		return settableBeanProperty;
	}

	@Override
	public Object createFromObjectWith(DeserializationContext ctxt, SettableBeanProperty[] props,
			PropertyValueBuffer buffer) throws JacksonException {
		return super.createFromObjectWith(ctxt, props, buffer);
	}
}

