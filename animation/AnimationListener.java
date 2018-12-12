package animation;

public interface AnimationListener {
	public void animationStarted(AnimationEvent e);
	public void animationStopped(AnimationEvent e);
	public void animationPaused(AnimationEvent e);
	public void animationFrameChanged(AnimationEvent e);
	public void animationPropertyChanged(AnimationEvent e);
}
