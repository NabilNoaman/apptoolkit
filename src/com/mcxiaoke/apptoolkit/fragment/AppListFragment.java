package com.mcxiaoke.apptoolkit.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mcxiaoke.apptoolkit.AppContext;
import com.mcxiaoke.apptoolkit.R;
import com.mcxiaoke.apptoolkit.adapter.AppListAdapter;
import com.mcxiaoke.apptoolkit.adapter.MultiChoiceArrayAdapter;
import com.mcxiaoke.apptoolkit.model.AppInfo;
import com.mcxiaoke.apptoolkit.task.AppListAsyncTask;
import com.mcxiaoke.apptoolkit.task.AsyncTaskCallback;
import com.mcxiaoke.apptoolkit.task.BackupAsyncTask;
import com.mcxiaoke.apptoolkit.task.SimpleAsyncTaskCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: filemanager
 * Package: com.com.mcxiaoke.appmanager.fragment
 * User: com.mcxiaoke
 * Date: 13-6-11
 * Time: 上午10:55
 */
public class AppListFragment extends BaseFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, MultiChoiceArrayAdapter.OnCheckedListener {
    private static final String TAG = AppListFragment.class.getSimpleName();

    private static void debug(String message) {
        AppContext.v(message);
    }

    private static final int MSG_PACKAGE_ADDED = 1001;
    private static final int MSG_PACKAGE_REMOVED = 1002;

    private ListView mListView;
    private List<AppInfo> mAppInfos;
    private MultiChoiceArrayAdapter<AppInfo> mArrayAdapter;
    private ActionModeCallback mActionModeCallback;
    private AppListAsyncTask mAsyncTask;
    private BackupAsyncTask mBackupTask;

    private ProgressDialog mProgressDialog;
    private ActionMode mActionMode;

    private boolean isBackuping;

    private Handler mUiHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContext.v("AppListFragment onCreate()");
        mAppInfos = new ArrayList<AppInfo>();
        setHasOptionsMenu(true);

        mUiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppContext.v("AppListFragment onCreateView()");
        View root = inflater.inflate(R.layout.fm_applist, null);
        mListView = (ListView) root.findViewById(android.R.id.list);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppContext.v("AppListFragment onActivityCreated()");
        mActionModeCallback = new ActionModeCallback(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mArrayAdapter = new AppListAdapter(getActivity(), mAppInfos);
        mArrayAdapter.setOnCheckedListener(this);
        mListView.setAdapter(mArrayAdapter);
        refresh();
    }

    @Override
    public void refresh() {
        AppContext.v("AppListFragment refresh()");
        startAsyncTask();
    }

    private void stopAsyncTask() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(false);
        }
    }

    private void startAsyncTask() {
        stopAsyncTask();
        mArrayAdapter.clear();
        AppContext.v("AppListFragment startAsyncTask()");
        mAsyncTask = new AppListAsyncTask(getActivity(), new SimpleAsyncTaskCallback<List<AppInfo>>() {
            @Override
            public void onTaskSuccess(int code, List<AppInfo> appInfos) {
                AppContext.v("AppListFragment onTaskSuccess() size is " + (appInfos == null ? "null" : appInfos.size()));
                hideProgressIndicator();
                mArrayAdapter.addAll(appInfos);
            }

            @Override
            public void onTaskFailure(int code, Throwable e) {
                AppContext.v("AppListFragment onTaskFailure()");
                hideProgressIndicator();
            }
        });
        mAsyncTask.start();
        showProgressIndicator();
    }

    private void showDialog(AppInfo app) {
        if (app != null) {

            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(DIALOG_TAG);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            AppActionDialogFragment newFragment = AppActionDialogFragment.newInstance(app);
            newFragment.show(ft, DIALOG_TAG);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppContext.v("AppListFragment onItemClick() position=" + position + " mActionMode=" + mActionMode);
        if (mActionMode != null) {
            mArrayAdapter.toggleChecked(position);
            checkActionMode();
        } else {
            final AppInfo app = mArrayAdapter.getItem(position);
            showDialog(app);
        }
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AppContext.v("AppListFragment onItemLongClick() position=" + position + " mActionMode=" + mActionMode);
        if (mActionMode == null) {
            mArrayAdapter.toggleChecked(position);
            checkActionMode();
        }
        return true;
    }

    @Override
    public void onCheckedChanged(int position, boolean isChecked) {
        checkActionMode();
    }

    private void checkActionMode() {
        if (mActionMode == null) {
            getSherlockActivity().startActionMode(mActionModeCallback);
        }
        setActionModeTitle();
    }

    private void setActionModeTitle() {
        if (mActionMode != null) {
            int checkedCount = mArrayAdapter.getCheckedItemCount();
            if (checkedCount == 0) {
                mActionMode.finish();
            } else {
                mActionMode.setTitle("选择应用");
                mActionMode.setSubtitle("已选择" + checkedCount + "项");
            }
        }
    }

    private boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    private void onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        mActionMode = mode;
        mArrayAdapter.setActionModeState(true);
        setActionModeTitle();
    }

    private boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    private void onDestroyActionMode(ActionMode mode) {
        mArrayAdapter.setActionModeState(false);
        mActionMode = null;
    }

    private static final String DIALOG_TAG = "DIALOG_TAG";

    static class ActionModeCallback implements ActionMode.Callback {
        private AppListFragment mFragment;

        public ActionModeCallback(AppListFragment fragment) {
            this.mFragment = fragment;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mFragment.onActionItemClicked(mode, item);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mFragment.onCreateActionMode(mode, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFragment.onDestroyActionMode(mode);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mFragment.onPrepareActionMode(mode, menu);
        }
    }


    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle(R.string.dialog_backup_title);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    AppContext.v("OnCancelListener");
                    stopBackup();
                }
            });
        }
        mProgressDialog.show();
    }

    private void updateProgressDialog(final String text) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.setMessage(text);
                }
            }
        });
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    private void showBackupConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_backup_all_title);
        builder.setMessage(R.string.dialog_backup_all_message);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startBackup();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showBackupCompleteDialog(int count) {
        String message = String.format(getString(R.string.dialog_backup_complete_message), mAppInfos.size(), count);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_backup_complete_title);
        builder.setMessage(message);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void startBackup() {
        if (isBackuping) {
            return;
        }
        AppContext.v("startBackup");
        stopBackup();
        isBackuping = true;
        mBackupTask = new BackupAsyncTask(getActivity(), new AsyncTaskCallback<String, Integer>() {

            @Override
            public void onTaskProgress(int code, String text) {
                AppContext.v("BackupAsyncTask.onTaskProgress " + text);
                updateProgressDialog(text);
            }

            @Override
            public void onTaskSuccess(int code, Integer integer) {
                AppContext.v("BackupAsyncTask.onTaskSuccess backup count is " + integer);
                int added = integer == null ? 0 : integer;
                dismissProgressDialog();
                showBackupCompleteDialog(added);
                isBackuping = false;
            }

            @Override
            public void onTaskFailure(int code, Throwable e) {
                AppContext.v("BackupAsyncTask.onTaskFailure ex is " + e);
                isBackuping = false;
                dismissProgressDialog();
            }
        });
        mBackupTask.start(mAppInfos);
        showProgressDialog();

    }

    private void stopBackup() {
        if (mBackupTask != null) {
            AppContext.v("stopBackup");
            mBackupTask.stop();
            mBackupTask = null;
            isBackuping = false;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
        stopAsyncTask();
        stopBackup();
    }

}
