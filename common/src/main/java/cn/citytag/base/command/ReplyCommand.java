package cn.citytag.base.command;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by yangfeng01 on 2017/11/27.
 */

public class ReplyCommand<T> {

	private Action execute0;	// 原Action0
	private Consumer<T> execute1;	// 原Action1

	//private Function<Object[], Boolean> canExecute0;

	public ReplyCommand(Action execute0) {
		this.execute0 = execute0;
	}

	public ReplyCommand(Consumer<T> execute1) {
		this.execute1 = execute1;
	}

	public ReplyCommand() {

	}

	///**
	// *
	// * @param execute0 callback for event
	// * @param canExecute0 if this function return true the action execute would be invoked! otherwise would't invoked!
	// */
	//public ReplyCommand(Action execute0, Function<Object[], Boolean> canExecute0) {
	//	this.execute0 = execute0;
	//	this.canExecute0 = canExecute0;
	//}

	///**
	// *
	// * @param execute1 callback for event,this callback need a params
	// * @param canExecute0 if this function return true the action execute would be invoked! otherwise would't invoked!
	// */
	//public ReplyCommand(Consumer<T> execute1, Function<Object[], Boolean> canExecute0) {
	//	this.execute1 = execute1;
	//	this.canExecute0 = canExecute0;
	//}

	public void execute() throws Exception {
		if (execute0 != null) {
			execute0.run();
		}
	}

	public void execute(T parameter) throws Exception {
		if (execute1 != null) {
			execute1.accept(parameter);
		}
	}


}
