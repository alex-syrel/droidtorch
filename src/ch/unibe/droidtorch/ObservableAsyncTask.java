package ch.unibe.droidtorch;

import android.os.AsyncTask;

/**
 * Simple AsyncTask wrapper that allows to get onTaskComplete message
 * when AsyncTask is completed
 * 
 * @param <INPUT>
 * @param <PROGRESS>
 * @param <RESULT>
 * 
 * @author Aliaksei Syrel
 */
public abstract class ObservableAsyncTask<INPUT,PROGRESS,RESULT> extends AsyncTask<INPUT,PROGRESS,RESULT>{

	private OnTaskCompletedListener<INPUT,PROGRESS,RESULT> mOnTaskCompleted;
	
	public interface OnTaskCompletedListener<INPUT,PROGRESS,RESULT> {
		public void onTaskCompleted(AsyncTask<INPUT,PROGRESS,RESULT> task);
	}
	
	public ObservableAsyncTask(){
		
	}
	
	public ObservableAsyncTask(OnTaskCompletedListener<INPUT,PROGRESS,RESULT> l){
		mOnTaskCompleted = l;
	}
		
	@Override
	protected void onPostExecute(RESULT result) {
		super.onPostExecute(result);
		if (mOnTaskCompleted != null) mOnTaskCompleted.onTaskCompleted(this);
	}
	
	public ObservableAsyncTask<INPUT,PROGRESS,RESULT> setOnTaskCompletedListener(OnTaskCompletedListener<INPUT,PROGRESS,RESULT> l){
		this.mOnTaskCompleted = l;
		return this;
	}

}
