package com.bille;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import com.bille.exceptions.BilleException;
import com.bille.exceptions.NoSuchStaffIDException;
import com.bille.ui.screens.*;
import com.bille.widgets.*;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QPropertyAnimation;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.QIODevice.OpenModeFlag;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class Application extends QFrame {

	private static Bille instance;

	public static void main(String[] args) {
		QApplication.initialize(args);
		
		if (!Application.lockInstance()) {
			QMessageBox.critical(mainWindow, "Error",
					"It seems like another instance<br />"
							+ "of the application is already running.");
			return;
		}

		Runtime.getRuntime().addShutdownHook(new ShutdownHook());

		try {
			Application.init();
		} catch (BilleException e) {
			QMessageBox.critical(mainWindow, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(mainWindow);
			return;
		}
				
		QApplication.exec();
	}

	private static boolean lockInstance() {
		try {
			socket = new ServerSocket(64432, 1, InetAddress.getLocalHost());
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public static void releaseInstance() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Failed to close app sokcet!");
				return;
			}
		}
	}
	
	public static String formatDecimal(double d) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		
		return nf.format(d);
	}
	
	public static void setSplashMessage(String msg) {
		splash.showMessage(msg);
	}

	public static void init() throws BilleException, SQLException {
		splashPixMap = new QPixmap("resources/splash.jpg");
		splash = new QSplashScreen(splashPixMap);
		splash.setStyleSheet("color: grey;");
		splash.show();
		
		/*
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			
		}*/
		
		instance = new Bille();
		instance.init();

		setSplashMessage("Building UI");
		initUI();
		setSplashMessage("Customizing UI");
		mainWindow.setStyleSheet(readStyleSheet());
		
		splash.finish(mainWindow);
		
		mainWindow.showFullScreen();
		
		if (hasExpired()) {
			showRenewLicenseScreen();
			return;
		}

		// TODO: For testing only. Remove afterwards.
		LoginScreen ls = (LoginScreen) currentWid;
		ls.hardLogin();
	}
	
	public static boolean hasExpired() {
		Date expires = instance.getStore().getExpires();
		Date now = new Date();
		
		if (now.compareTo(expires) > 0) {
			return true;
		}
		
		return false;
	}

	private static void initUI() {
		// Construct widgets
		mainWindow = new QFrame();
		mainLayout = new QVBoxLayout(mainWindow);
		locationLab = new QLabel(mainWindow);

		centralWid = new QWidget(mainWindow);
		centralLayout = new QHBoxLayout(centralWid);
		scrollArea = new QScrollArea(centralWid);
		wrapperWid = new QWidget(scrollArea);
		wrapperLay = new QVBoxLayout(wrapperWid);

		// Set up initial screen (login)
		currentWid = new LoginScreen(wrapperWid);

		wrapperLay.addWidget(currentWid, 0, new Alignment(
				AlignmentFlag.AlignCenter));
		wrapperWid.setLayout(wrapperLay);

		scrollArea.setWidget(wrapperWid);
		scrollArea.setProperty("has_round_border", true);
		scrollArea.setWidgetResizable(true);

		// Add stuff to central widget
		centralLayout.addWidget(scrollArea);
		centralWid.setLayout(centralLayout);

		// Add stuff to main layout
		locationLab.setText("Login");
		locationLab.setProperty("is_location", true);

		mainLayout.addWidget(new Header(mainWindow));
		mainLayout.addWidget(locationLab, 0, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLayout.addWidget(centralWid);
		mainLayout.addWidget(new Footer(mainWindow));
		mainWindow.setLayout(mainLayout);

		mainWindow.setWindowTitle("bille");
	}

	private static String readStyleSheet() {
		QFile file = new QFile("resources/theme.css");
		file.open(OpenModeFlag.ReadOnly);
		String stylesheet = file.readAll().toString();
		file.close();
		return stylesheet;
	}

	public static void showSystemError(QWidget parent) {
		QMessageBox.critical(parent, "System error",
				"There was an unexpected error while per"
						+ "forming the operation");
	}

	public static Bille getInstance() {
		return instance;
	}

	public static QWidget getMainWidget() {
		return mainWindow;
	}
	
	// Screen functions

	public static void showMenuBar() {
		menuBar = new MenuBar(centralWid);
		centralLayout.addWidget(menuBar, 0, new Alignment(
				AlignmentFlag.AlignRight));
	}

	private static void animateCurrentWidget() {
		QPropertyAnimation animation = new QPropertyAnimation(currentWid,
				new QByteArray("geometry"));

		int x = (scrollArea.width() - currentWid.sizeHint().width()) / 2;
		int y = ((scrollArea.height() - currentWid.sizeHint().height()) / 2);

		animation.setKeyValueAt(0, new QRect(0, y, currentWid.sizeHint()
				.width(), currentWid.sizeHint().height()));
		animation.setKeyValueAt(1, new QRect(x, y, currentWid.sizeHint()
				.width(), currentWid.sizeHint().height()));
		animation.setDuration(200);
		animation.setLoopCount(1);
		currentWid.show();
		// animation.start();
	}

	private static void showScreen(QWidget w, String loc, boolean central) {
		currentWid.hide();
		wrapperLay.removeWidget(currentWid);

		w.hide();

		if (central) {
			wrapperLay
					.addWidget(w, 0, new Alignment(AlignmentFlag.AlignCenter));
		} else {
			wrapperLay.addWidget(w, 0);
		}

		locationLab.setText(loc);

		currentWid.disposeLater();
		currentWid = w;
		animateCurrentWidget();
	}
	
	public static void showRenewLicenseScreen() {
		showScreen(new RenewLicenseScreen(mainWindow), "Renew license", true);
	}

	public static void showRestoNewOrderScreen() {
		restoNewOrderScreen = new RestoNewOrderScreen(wrapperWid);
		showScreen(restoNewOrderScreen, "New order", false);
	}

	public static void showRetailNewOrderScreen() {
		retailNewOrderScreen = new RetailNewOrderScreen(wrapperWid);
		showScreen(retailNewOrderScreen, "New order", false);
	}

	public static void showNewOrderScreen() {
		if (hasExpired()) {
			logout();
			showRenewLicenseScreen();
			return;
		}
		
		if (instance.getStore().getType() == StoreType.RESTO) {
			showRestoNewOrderScreen();
		} else if (instance.getStore().getType() == StoreType.RETAIL) {
			showRetailNewOrderScreen();
		}
	}

	public static void showHomeScreen() {
		if (hasExpired()) {
			logout();
			showRenewLicenseScreen();
			return;
		}
		
		homeScreen = new HomeScreen(wrapperWid);
		showScreen(homeScreen, "Home", true);
	}

	public static void showActiveOrdersScreen() {
		activeOrdersScreen = new ActiveOrdersScreen(wrapperWid);
		showScreen(activeOrdersScreen, "Active orders", false);
	}

	public static void showEditActiveOrderScreen(RestoOrder o) {
		editActiveOrderScreen = new EditActiveOrderScreen(wrapperWid, o);
		showScreen(editActiveOrderScreen, "Edit Order", false);
	}

	public static void showCatScreen() {
		categoriesScreen = new CategoriesScreen(wrapperWid);
		categoriesScreen.show();
	}

	public static void showItemsScreen() {
		itemsScreen = new ItemsScreen(wrapperWid);
		itemsScreen.show();
	}

	public static void showProductsScreen() {
		productsScreen = new ProductsScreen(wrapperWid);
		productsScreen.show();
	}

	public static void showTaxesScreen() {
		taxesScreen = new TaxesScreen(wrapperWid);
		taxesScreen.show();
	}

	public static void showCustomersScreen() {
		customersScreen = new CustomersScreen(wrapperWid);
		customersScreen.show();
	}

	public static void showStaffScreen() {
		staffScreen = new StaffScreen(wrapperWid);
		staffScreen.show();
	}

	public static void showSettingsScreen() {
		settingsScreen = new SettingsScreen(wrapperWid);
		showScreen(settingsScreen, "Settings", true);
	}
	
	public static void showLoginScreen() {
		loginScreen = new LoginScreen(wrapperWid);
		showScreen(loginScreen, "Login", true);
	}

	public static void logout() {
		instance.setLoggedInStaff(null);

		menuBar.hide();
		centralLayout.removeWidget(menuBar);

		loginScreen = new LoginScreen(wrapperWid);
		showScreen(loginScreen, "Login", true);
	}

	private static ServerSocket socket;
	
	private static QPixmap splashPixMap;
	private static QSplashScreen splash;

	private static MenuBar menuBar;
	private static LoginScreen loginScreen;
	private static HomeScreen homeScreen;
	private static RetailNewOrderScreen retailNewOrderScreen;
	private static RestoNewOrderScreen restoNewOrderScreen;
	private static ActiveOrdersScreen activeOrdersScreen;
	private static EditActiveOrderScreen editActiveOrderScreen;
	private static CategoriesScreen categoriesScreen;
	private static ItemsScreen itemsScreen;
	private static ProductsScreen productsScreen;
	private static TaxesScreen taxesScreen;
	private static CustomersScreen customersScreen;
	private static StaffScreen staffScreen;
	private static SettingsScreen settingsScreen;

	private static QFrame mainWindow;

	private static QVBoxLayout mainLayout;
	private static QWidget currentWid;
	private static QLabel locationLab;

	private static QWidget centralWid;
	private static QHBoxLayout centralLayout;
	private static QScrollArea scrollArea;
	private static QWidget wrapperWid;
	private static QVBoxLayout wrapperLay;

}
