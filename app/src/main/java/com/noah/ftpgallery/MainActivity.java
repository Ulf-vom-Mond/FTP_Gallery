package com.noah.ftpgallery;

import android.Manifest;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.noah.ftpgallery.ui.generalSettings.GeneralSettingsActivity;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements serverSettings.Communication {

	private AppBarConfiguration mAppBarConfiguration;
	private static String lastSelectedServer = null;
	private Menu mainDrawerMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		NavigationView navigationView = findViewById(R.id.nav_view);


		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(
				R.id.nav_home, R.id.nav_settings)
				.setDrawerLayout(drawer)
				.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

		setupWithNavController(navigationView, navController);
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 0);
		}
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
		}
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
		}
		File configFile = new File(getFilesDir() + "/connectionSettings.ser");
		if(!configFile.exists()){
			ArrayList<Connection> connectionSettings = new ArrayList<Connection>();
			try {
				FileOutputStream fileOut = new FileOutputStream(getFilesDir() + "/connectionSettings.ser");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(connectionSettings);
				out.close();
				fileOut.close();
				//Log.i("yeet", "created and serialized empty arrayList");
			} catch (IOException i) {
				i.printStackTrace();
			}
		}
		ArrayList<Connection> connectionSettings = new ArrayList<>();
		try {
			FileInputStream fileIn = new FileInputStream(getFilesDir() + "/connectionSettings.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			connectionSettings = (ArrayList<Connection>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return;
		}
		mainDrawerMenu = navigationView.getMenu();
		for (int i = 0; i < connectionSettings.size(); i++) {
			mainDrawerMenu.add(R.id.addServerGroup, R.id.nav_serverSettings, 0, connectionSettings.get(i).getConnectionName());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
	}



	public void setupWithNavController(@NonNull final NavigationView navigationView,
											  @NonNull final NavController navController) {
		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(@NonNull MenuItem item) {
						Log.i("yeet", "magiccccc! " + item.toString());
						lastSelectedServer = item.toString();
						boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
						if (handled) {
							ViewParent parent = navigationView.getParent();
							if (parent instanceof DrawerLayout) {
								((DrawerLayout) parent).closeDrawer(navigationView);
							} else {
								BottomSheetBehavior bottomSheetBehavior =
										findBottomSheetBehavior(navigationView);
								if (bottomSheetBehavior != null) {
									bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
								}
							}
						}
						return handled;
					}
				});
		final WeakReference<NavigationView> weakReference = new WeakReference<>(navigationView);
		navController.addOnDestinationChangedListener(
				new NavController.OnDestinationChangedListener() {
					@Override
					public void onDestinationChanged(@NonNull NavController controller,
													 @NonNull NavDestination destination, @Nullable Bundle arguments) {
						NavigationView view = weakReference.get();
						if (view == null) {
							navController.removeOnDestinationChangedListener(this);
							return;
						}
						Menu menu = view.getMenu();
						for (int h = 0, size = menu.size(); h < size; h++) {
							MenuItem item = menu.getItem(h);
							item.setChecked(matchDestination(destination, item.getItemId()));
						}
					}
				});
	}

	static BottomSheetBehavior findBottomSheetBehavior(@NonNull View view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (!(params instanceof CoordinatorLayout.LayoutParams)) {
			ViewParent parent = view.getParent();
			if (parent instanceof View) {
				return findBottomSheetBehavior((View) parent);
			}
			return null;
		}
		CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
				.getBehavior();
		if (!(behavior instanceof BottomSheetBehavior)) {
			// We hit a CoordinatorLayout, but the View doesn't have the BottomSheetBehavior
			return null;
		}
		return (BottomSheetBehavior) behavior;
	}

	static boolean matchDestination(@NonNull NavDestination destination,
									@IdRes int destId) {
		NavDestination currentDestination = destination;
		while (currentDestination.getId() != destId && currentDestination.getParent() != null) {
			currentDestination = currentDestination.getParent();
		}
		return currentDestination.getId() == destId;
	}

	@Override
	public String getSelectedServer() {
		return lastSelectedServer;
	}

	@Override
	public Menu getMainDrawerMenu() {
		return mainDrawerMenu;
	}
}
