package averan4.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author codistmonk (creation 2014-12-20)
 */
public final class Session implements Serializable {
	
	private final List<Frame> frames;
	
	public Session() {
		this.frames = new ArrayList<>();
	}
	
	public final List<Frame> getFrames() {
		return this.frames;
	}
	
	public final Session prove(final String factName, final Expression<?> fact) {
		this.getFrames().add(this.new Frame(factName, fact));
		
		return this;
	}
	
	public final Session cancelFrame() {
		this.getFrames().remove(this.getFrames().size() - 1);
		
		return this;
	}
	
	public final void cancelSession() {
		throw new RuntimeException();
	}
	
	public final Session accept(final String... factNames) {
		final Frame frame = this.getFrames().get(this.getFrames().size() - 1);
		
		if (frame.getGoal() == null) {
			// TODO
		} else {
			// TODO
		}
		
		return this;
	}
	
	public final Session introduce() {
		// TODO
		return this.accept();
	}
	
	public final Session suppose(final String conditionName, final Expression<?> conditionProposition) {
		// TODO
		return this.accept();
	}
	
	public final Session apply(final String factName, final String moduleName, final String conditionName) {
		// TODO
		return this.accept();
	}
	
	public final Session substitute(final String factName, final Expression<?> expression, final Composite<?>... equalities) {
		// TODO
		return this.accept();
	}
	
	public final Session rewrite(final String factName, final String propositionName, final String equalityName,
			final int... indices) {
		// TODO
		return this.accept();
	}
	
	public final Module getCurrentModule() {
		final Frame currentFrame = this.getCurrentFrame();
		
		return currentFrame == null ? null : currentFrame.getModule();
	}
	
	public final Frame getCurrentFrame() {
		return this.getFrames().isEmpty() ? null : this.getFrames().get(this.getFrames().size() - 1);
	}
	
	/**
	 * @author codistmonk (creation 2014-12-20)
	 */
	public final class Frame implements Serializable {
		
		private final String name;
		
		private final Module module;
		
		private Expression<?> goal;
		
		public Frame(final String name, final Expression<?> goal) {
			this.name = name;
			this.module = new Module(getCurrentModule());
			this.goal = goal;
		}
		
		public final String getName() {
			return this.name;
		}
		
		public final Module getModule() {
			return this.module;
		}
		
		public final Expression<?> getGoal() {
			return this.goal;
		}
		
		private static final long serialVersionUID = -5943416769824876039L;
		
	}
	
	private static final long serialVersionUID = 181621455530572267L;
	
}
