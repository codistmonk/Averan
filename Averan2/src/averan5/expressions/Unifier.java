package averan5.expressions;

import static averan5.expressions.Expressions.toTreeSet;
import static net.sourceforge.aprog.tools.Tools.cast;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.sourceforge.aprog.tools.Pair;
import net.sourceforge.aprog.tools.Tools;

/**
 * @author codistmonk (creation 2015-04-19)
 */
public final class Unifier implements Serializable {
	
	private final String preferredName;
	
	private Collection<Unifier> unifiers;
	
	private Object[] object;
	
	private Unifier(final String preferredName, final Collection<Unifier> unifiers, final Object[] object) {
		this.preferredName = preferredName;
		this.unifiers = unifiers;
		this.object = object;
	}
	
	public Unifier(final String preferredName) {
		this(preferredName, new HashSet<>(), new Object[1]);
		
		this.unifiers.add(this);
	}
	
	public final Object getObject() {
		return this.object[0];
	}
	
	public final void snapshotTo(final Map<Unifier, Pair<Unifier, Unifier>> snapshot) {
		for (final Unifier unifier : this.unifiers) {
			snapshot.computeIfAbsent(unifier, Unifier::snapshot);
		}
	}
	
	public final Pair<Unifier, Unifier> snapshot() {
		final Unifier structure = new Unifier(this.preferredName, this.unifiers, this.object);
		final Unifier contents = new Unifier(null);
		contents.unifiers.clear();
		contents.unifiers.addAll(this.unifiers);
		contents.object[0] = this.getObject();
		
		return new Pair<>(structure, contents);
	}
	
	public final Unifier restore(final Pair<Unifier, Unifier> snapshot) {
		final Unifier structure = snapshot.getFirst();
		final Unifier contents = snapshot.getSecond();
		this.unifiers = structure.unifiers;
		this.object = structure.object;
		this.unifiers.clear();
		this.unifiers.addAll(contents.unifiers);
		this.object[0] = contents.getObject();
		
		return this;
	}
	
	public final boolean unifies(final Object object) {
		if (this == object) {
			return true;
		}
		
		if (object == null) {
			return false;
		}
		
		final Object thisObject = this.getObject();
		final Unifier that = cast(this.getClass(), object);
		
		if (that != null) {
			final Object thatObject = that.getObject();
			final boolean merge = thisObject != null && thatObject != null;
			
			if (merge && !thisObject.equals(thatObject)) {
				return false;
			}
			
			final Unifier absorber, absorbed;
			
			if (merge || thatObject == null) {
				absorber = this;
				absorbed = that;
			} else {
				absorber = that;
				absorbed = this;
			}
			
			for (final Unifier unifier : absorbed.unifiers) {
				absorber.unifiers.add(unifier);
				unifier.unifiers = absorber.unifiers;
				unifier.object = absorber.object;
			}
			
			return true;
		}
		
		{
			if (thisObject == null) {
				this.object[0] = object;
				
				return true;
			}
			
			return Tools.equals(thisObject, object);
		}
	}
	
	@Override
	public final String toString() {
		String prefix = null;
		
		if (this.unifiers.size() == 1) {
			prefix = this.unifiers.iterator().next().preferredName;
		}
		
		if (prefix == null) {
			prefix = this.unifiers.stream().map(u -> ids.computeIfAbsent(u, k -> id.incrementAndGet())).collect(toTreeSet()).toString();
		}
		
		if (debug.get() && busy.add(this)) {
			try {
				return prefix + "{" + (this.getObject() != null ? this.getObject() : "") + "}";
			} finally {
				busy.remove(this);
			}
		}
		
		return prefix;
	}
	
	private static final long serialVersionUID = 4343191681740782407L;
	
	private static final  AtomicInteger id = new AtomicInteger();
	
	private static final Map<Unifier, Integer> ids = new WeakHashMap<>();
	
	private static final Collection<Object> busy = new HashSet<>();
	
	public static final AtomicBoolean debug = new AtomicBoolean(false);
	
}
