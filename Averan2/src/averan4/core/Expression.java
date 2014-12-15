package averan4.core;

import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author codistmonk (creation 2014-12-15)
 */
public abstract interface Expression extends Serializable {
	
	public abstract Module getContext();
	
	public abstract String getPropositionName();
	
	public default <R> R accept(final Visitor<R> visitor) {
		return visitor.visit(this);
	}
	
	public default Expression evaluate() {
		return this;
	}
	
	/**
	 * @author codistmonk (creation 2014-12-15)
	 */
	public static abstract interface Visitor<R> extends Serializable {
		
		public abstract R visit(Expression expression);
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-15)
	 */
	public static abstract class Default implements Expression {
		
		private final Module context;
		
		private final String propositionName;
		
		protected Default(final Module context, final String propositionName) {
			this.context = context;
			this.propositionName = propositionName;
		}
		
		@Override
		public final Module getContext() {
			return this.context;
		}
		
		@Override
		public final String getPropositionName() {
			return this.propositionName;
		}
		
		private static final long serialVersionUID = 8887123479474009959L;
		
	}
	
	/**
	 * @author codistmonk (creation 2014-12-15)
	 */
	public static final class Module extends Default {
		
		private final List<Expression> elements;
		
		private final Map<String, Expression> bindings;
		
		public Module(final Module context, final String propositionName) {
			this(context, propositionName, new ArrayList<>());
		}
		
		public Module(final Module context, final String propositionName, final List<Expression> elements) {
			super(context, propositionName);
			this.elements = elements;
			this.bindings = new HashMap<>();
		}
		
		public final List<Expression> getElements() {
			return this.elements;
		}
		
		public final Map<String, Expression> getBindings() {
			return this.bindings;
		}
		
		@Override
		public final Expression evaluate() {
			application.getBindings().clear();
			
			if (application.equals(this)) {
				// TODO bind variables
				return application.getBindings().get("q");
			}
			
			substitution.getBindings().clear();
			
			if (substitution.equals(this)) {
				final Module equalites = cast(Module.class, substitution.getBindings().get("S"));
				
				if (equalites != null && equalites.isEqualities()) {
					// TODO
				}
			}
			
			return this;
		}
		
		public final boolean isEqualities() {
			for (final Expression element : this.getElements()) {
				final Module m = cast(Module.class, element);
				
				if (m.getElements().size() != 3 || !EQUALS.equals(m.getElements().get(1))) {
					return false;
				}
			}
			
			return true;
		}
		
		@Override
		public final int hashCode() {
			return this.getElements().hashCode();
		}
		
		@Override
		public final boolean equals(final Object object) {
			if (object == this) {
				return true;
			}
			
			final Module that = cast(this.getClass(), object);
			
			return that != null && this.getElements().equals(that.getElements());
		}
		
		@Override
		public final String toString() {
			return this.getElements().toString();
		}
		
		/**
		 * @author codistmonk (creation 2014-12-15)
		 */
		public final class Symbol extends Default {
			
			private final String string;
			
			public Symbol(final String string) {
				this(null, string);
			}
			
			public Symbol(final String propositionName, final String string) {
				super(Module.this, propositionName);
				this.string = string;
			}
			
			@Override
			public final int hashCode() {
				return this.getContext().hashCode() + this.toString().hashCode();
			}
			
			@Override
			public final boolean equals(final Object object) {
				if (object == this) {
					return true;
				}
				
				final Symbol that = cast(this.getClass(), object);
				
				return that != null && this.getContext() == that.getContext() && this.toString().equals(that.toString());
			}
			
			@Override
			public final String toString() {
				return this.string;
			}
			
			private static final long serialVersionUID = -1850503219849209892L;
			
		}
		
		/**
		 * @author codistmonk (creation 2014-12-15)
		 */
		public final class Variable extends Default {
			
			private final String name;
			
			public Variable(final String name) {
				this(null, name);
			}
			
			public Variable(final String propositionName, final String name) {
				super(Module.this, propositionName);
				this.name = name;
			}
			
			public final String getName() {
				return this.name;
			}
			
			public final Variable clearBinding() {
				this.getBindings().remove(this.getName());
				
				return this;
			}
			
			public final Expression getValue() {
				return this.getBindings().get(this.getName());
			}
			
			public final Map<String, Expression> getBindings() {
				return this.getContext().getBindings();
			}
			
			@Override
			public final int hashCode() {
				return this.getName().hashCode();
			}
			
			@Override
			public final boolean equals(final Object object) {
				if (object == this) {
					return true;
				}
				
				{
					final Variable that = cast(this.getClass(), object);
					
					if (that != null && this.getContext() == that.getContext() && this.getName().equals(that.getName())) {
						return true;
					}
				}
				
				{
					final Expression that = cast(Expression.class, object);
					
					if (that == null) {
						return false;
					}
					
					final Map<String, Expression> bindings = this.getBindings();
					final Expression value = bindings.get(this.getName());
					
					if (value == null) {
						bindings.put(this.getName(), that);
						
						return true;
					}
					
					return value.equals(object);
				}
			}
			
			@Override
			public final String toString() {
				return this.getName() + "?";
			}
			
			private static final long serialVersionUID = -7271244144130122065L;
			
		}
		
		private static final long serialVersionUID = 6115372359748959087L;
		
		public static final Module ROOT = new Module(null, "Root");
		
		public static final Symbol APPLY = ROOT.new Symbol("Apply");
		
		public static final Symbol SUBSTITUTE = ROOT.new Symbol("Substitute");
		
		public static final Symbol IMPLIES = ROOT.new Symbol("->");
		
		public static final Symbol EQUALS = ROOT.new Symbol("=");
		
		static final Module application = new Module(ROOT, "Application");
		
		static final Module substitution = new Module(ROOT, "Substitution");
		
		static {
			{
				final Variable p = application.new Variable("P");
				final Variable q = application.new Variable("Q");
				
				application.getElements().add(APPLY);
				application.getElements().add(p);
				application.getElements().add(new Module(application, null, Arrays.asList(p, IMPLIES, q)));
			}
			
			{
				final Variable p = substitution.new Variable("P");
				final Variable s = substitution.new Variable("S");
				
				substitution.getElements().add(APPLY);
				substitution.getElements().add(p);
				substitution.getElements().add(s);
			}
		}
		
	}
	
}
