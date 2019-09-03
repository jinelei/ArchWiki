package cn.jinelei.smart.archwiki;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		initView();
	}

	private void initView() {
		ActionBar supportActionBar = getSupportActionBar();
		supportActionBar.setTitle(R.string.about);
		supportActionBar.setDisplayHomeAsUpEnabled(true);
		try {
			((TextView) findViewById(R.id.tv_app_name)).setText("" + getPackageManager().getPackageInfo(getPackageName(), 0).packageName);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			((TextView) findViewById(R.id.tv_app_name)).setText(R.string.app_name);
		}
		try {
			((TextView) findViewById(R.id.tv_app_version)).setText("" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			((TextView) findViewById(R.id.tv_app_version)).setText("1.0.1");
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

}
