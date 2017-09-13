package fr.ligorax.Dixio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//***********************************************************************
// MainActivity
//***********************************************************************
public class MainActivity extends FragmentActivity implements View.OnClickListener
{
	// CONSTANTS
	private static final String TAG = "MainActivity";
	private static Date buildDate = null;
	public Context ZeContext;
	public ArrayList<String> WordsList = new ArrayList<String>();
	FindWords getWords;
	FindWordOnline findWordOnline;
	ArrayList<ImageView> anaWordLetters = new ArrayList<>();
	ArrayList<ImageView> anaGuessLetters = new ArrayList<>();
	// Variables
	private TabHost myTabHost;
	private Menu actionBarMenu;
	private ODSLib odsLib;
	private SharedPreferences prefs;
	//
	private int prefODS;
	private int prefOnlineDico;
	//
	private int anaLevel = 7;
	private int anaTime = 3;
	private int anaColumn = 0;
	private String anaWord;
	private String anaGuess;
	private int anaScore = 0;
	private ArrayList<String> anaWords;
	//

	//***********************************************************************
	// onClick()
	//***********************************************************************
	@Override
	public void onClick(View v)
	{
		int clicked_id = v.getId(); // here you get id for clicked TableRow
		// now you can get value like this
		String word = getWords.words.get(clicked_id);
		showWord(v, word);
		myTabHost.setCurrentTab(1);
	}

	//***********************************************************************
	// onCreateOptionsMenu()
	//***********************************************************************
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Display the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar, menu);
		actionBarMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	//***********************************************************************
	// onOptionsItemSelected()
	//***********************************************************************
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Manage the action bar
		switch (item.getItemId())
		{
			case R.id.overflow:
				Intent intent = new Intent(this, PrefsActivity.class);
				startActivity(intent);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	//***********************************************************************
	// onCreate()
	//***********************************************************************
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		initApp();

		setContentView(R.layout.main);
		myTabHost = (TabHost) findViewById(R.id.TabHost01);
		myTabHost.setup();
		//
		myTabHost.addTab(myTabHost.newTabSpec("idSearch").setIndicator("", getResources().getDrawable(R.drawable.ic_dixio_explore)).setContent(R.id.Search));
		myTabHost.addTab(myTabHost.newTabSpec("idWord").setIndicator("", getResources().getDrawable(R.drawable.ic_dixio_checkword)).setContent(R.id.Word));
		myTabHost.addTab(myTabHost.newTabSpec("idAnagram").setIndicator("", getResources().getDrawable(R.drawable.ic_dixio_anagram)).setContent(R.id.Anagrams));
		myTabHost.addTab(myTabHost.newTabSpec("idLongestWord").setIndicator("", getResources().getDrawable(R.drawable.ic_dixio_longest_word)).setContent(R.id.LongestWord));
		myTabHost.addTab(myTabHost.newTabSpec("idInfo").setIndicator("", getResources().getDrawable(R.drawable.ic_dixio_info)).setContent(R.id.Info));
		//
		myTabHost.setCurrentTab(0);
		// Tab change Listener
		myTabHost.setOnTabChangedListener(
				new TabHost.OnTabChangeListener()
				{
					public void onTabChanged(String tabId)
					{
						switch (tabId)
						{
							case "idSearch":
								break;

							case "idWord":
								break;

							case "idAnagram":
								showAnagrams();
								break;

							case "idLongestWord":
								break;

							case "idInfo":
								showInfo();
								break;
						}
					}
				}
		);

		//******************************************************************************************
		// TAB SEARCH : Map functions to events
		//******************************************************************************************
		// Set edit to uppercase
		EditText txtDraught = (EditText) findViewById(R.id.txtDraught);
		txtDraught.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

		// Find button
		ImageView btnFind = (ImageView) findViewById(R.id.btnFind);
		btnFind.setOnClickListener(
				new Button.OnClickListener()
				{
					public void onClick(View v)
					{
						// Hide the keyboard
						EditText txtDraught = (EditText) findViewById(R.id.txtDraught);
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(txtDraught.getWindowToken(), 0);
						// Get the draught from input text field
						String draught = txtDraught.getText().toString();
						if (draught != null && !draught.isEmpty())
						{
							odsLib.setDraught(draught);
							// Search for words
							getWords = new FindWords((Activity) v.getContext());
							getWords.execute(odsLib);
						}
					}
				}
		);

		// New random draught button
		ImageView btnRefresh = (ImageView) findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(
				new Button.OnClickListener()
				{
					public void onClick(View v)
					{
						odsLib.setDraught(odsLib.getRandomDraught(odsLib.DRAUGHT_SCRABBLE_NO_JOKERS, 7));
						TextView txtDraught = (TextView) findViewById(R.id.txtDraught);
						txtDraught.setText(odsLib.draught);
					}
				}
		);

		// Alpha sort button
		ImageView btnWordsSortAlpha = (ImageView) findViewById(R.id.btnWordsSortAlpha);
		btnWordsSortAlpha.setOnClickListener(
				new Button.OnClickListener()
				{
					public void onClick(View v)
					{
						TableLayout tbl = (TableLayout) findViewById(R.id.tblWords);

						while (tbl.getChildCount() > 1)
							tbl.removeView(tbl.getChildAt(tbl.getChildCount() - 1));

						WordsList = new ArrayList<String>();
						WordsList = getWords.words;
						ArrayList<String> sortedList = new ArrayList<String>();
						sortedList = ODSLib.sortWords(WordsList, ODSLib.SORT_ALPHA);
						addWordsToTable(tbl, sortedList);
					}
				}
		);

		// Length sort button
		ImageView btnWordsSortLength = (ImageView) findViewById(R.id.btnWordsSortLength);
		btnWordsSortLength.setOnClickListener(
				new Button.OnClickListener()
				{
					public void onClick(View v)
					{
						TableLayout tbl = (TableLayout) findViewById(R.id.tblWords);

						while (tbl.getChildCount() > 1)
							tbl.removeView(tbl.getChildAt(tbl.getChildCount() - 1));

						WordsList = new ArrayList<String>();
						WordsList = getWords.words;
						ArrayList<String> sortedList = new ArrayList<String>();
						sortedList = ODSLib.sortWords(WordsList, ODSLib.SORT_LENGTH);
						addWordsToTable(tbl, sortedList);
					}
				}
		);

		// Value sort button
		ImageView btnWordsSortValue = (ImageView) findViewById(R.id.btnWordsSortValue);
		btnWordsSortValue.setOnClickListener(
				new Button.OnClickListener()
				{
					public void onClick(View v)
					{
						TableLayout tbl = (TableLayout) findViewById(R.id.tblWords);

						while (tbl.getChildCount() > 1)
							tbl.removeView(tbl.getChildAt(tbl.getChildCount() - 1));

						WordsList = new ArrayList<String>();
						WordsList = getWords.words;

						ArrayList<String> sortedList = new ArrayList<String>();
						sortedList = ODSLib.sortWords(WordsList, ODSLib.SORT_VALUE);
						addWordsToTable(tbl, sortedList);
					}
				}
		);

		//******************************************************************************************
		// TAB WORD : Map functions to events
		//******************************************************************************************
		// Set edit to uppercase
		EditText txtSearchWord = (EditText) findViewById(R.id.txtSearchWord);
		txtSearchWord.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

		// Find button
		ImageView btnSearchWord = (ImageView) findViewById(R.id.btnSearchWord);
		btnSearchWord.setOnClickListener(
				new Button.OnClickListener()
				{
					public void onClick(View v)
					{
						TextView txtWord = (TextView) findViewById(R.id.txtSearchWord);
						String word = txtWord.getText().toString();
						if (word != null && !word.isEmpty())
						{
							runSearchOnline(v, txtWord.getText().toString());
						}
					}
				}
		);
	}

	//***********************************************************************
	// runSearchOnline()
	//***********************************************************************
	private void runSearchOnline(View v, String word)
	{
		TextView txtWord = (TextView) findViewById(R.id.txtSearchWord);
		txtWord.setText(word);
		// Hide the keyboard
		EditText txtSearchWord = (EditText) findViewById(R.id.txtSearchWord);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtSearchWord.getWindowToken(), 0);
		// Search for word online
		findWordOnline = new FindWordOnline((Activity) v.getContext());
		findWordOnline.execute(odsLib);
	}

	//***********************************************************************
	// onSaveInstanceState()
	//***********************************************************************
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean("fragment_added", true);
	}

	//***********************************************************************
	// initApp()
	//***********************************************************************
	private void initApp()
	{
		this.ZeContext = getBaseContext();
		// Get preferred location from Preferences screen
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefODS = Integer.parseInt(prefs.getString("prefODS", "7"));
		prefOnlineDico = Integer.parseInt(prefs.getString("prefOnlineDico", "0"));

		// Open the ODSLib
		odsLib = new ODSLib(this.ZeContext, prefODS);

		// Get Build date
		buildDate = getClassBuildTime();
	}

	//***********************************************************************
	// addWordsToTable()
	//***********************************************************************
	private void addWordsToTable(TableLayout tb, ArrayList<String> words)
	{
		int c;
		int i = 0;
		for (String word : words)
		{
			if (i % 2 == 0)
			{
				c = getResources().getColor(R.color.blueline1);
			} else
			{
				c = getResources().getColor(R.color.blueline2);
			}
			tb.setColumnShrinkable(1, true);
			TableRow row = new TableRow(this);
			TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
			row.setLayoutParams(lp);
			row.setPadding(0, 0, 0, 0);
			// Word
			TextView tvWord = new TextView(this);
			tvWord.setPadding(200, 0, 50, 0);
			tvWord.setGravity(Gravity.RIGHT);
			tvWord.setBackgroundColor(c);
			tvWord.setTypeface(null, Typeface.BOLD);
			tvWord.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			tvWord.setText(word);
			// Length
			TextView tvLength = new TextView(this);
			tvLength.setPadding(50, 0, 50, 0);
			tvLength.setGravity(Gravity.RIGHT);
			tvLength.setBackgroundColor(c);
			tvLength.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			tvLength.setText(Integer.toString(word.length()));
			// Value
			TextView tvValue = new TextView(this);
			tvValue.setPadding(50, 0, 200, 0);
			tvValue.setGravity(Gravity.RIGHT);
			tvValue.setBackgroundColor(c);
			tvValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			tvValue.setText(Integer.toString(odsLib.getWordValue(word)));
			// Add all these labels to row
			row.addView(tvWord);
			row.addView(tvLength);
			row.addView(tvValue);
			//
			row.setId(i); // here you can set unique id to TableRow for
			// identification
			row.setOnClickListener(MainActivity.this); // set TableRow onClickListner
			// Add row to inner table
			tb.addView(row);
			i++;
		}

	}

	//***********************************************************************
	// anaNewWord()
	//***********************************************************************
	private void anaNewWord()
	{
		ImageView btnAnaRefresh = (ImageView) findViewById(R.id.btnAnaRefresh);
		// Hide the keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(btnAnaRefresh.getWindowToken(), 0);
		//
		Spinner spnLevel = (Spinner) findViewById(R.id.spnLevel);
		Spinner spnTime = (Spinner) findViewById(R.id.spnTime);
		String x = spnLevel.getSelectedItem().toString();
		Log.i(TAG, "SELECTED ITEM : " + x);
		anaLevel = Integer.parseInt(x.substring(0, 1));
		x = spnTime.getSelectedItem().toString();
		if (x.substring(0, 1).equals("Pa"))
			anaTime = 0;
		else
			anaTime = Integer.parseInt(x.substring(0, 1));
		TableRow rowWord = (TableRow) findViewById(R.id.rowWord);
		rowWord.removeAllViews();
		//
		final TableRow rowGuess = (TableRow) findViewById(R.id.rowGuess);
		rowGuess.removeAllViews();
		//
		TextView txtAnaScore = (TextView) findViewById(R.id.txtAnaScore);
		txtAnaScore.setText(Integer.toString(anaScore));
		//
		anaColumn = 0;
		anaWord = odsLib.shuffleWord(odsLib.getRandomWord(anaLevel));
		anaWords = odsLib.findAnagrams(anaWord);
		anaGuess = "";
		Log.i(TAG, anaWord);
		btnAnaRefresh.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				for (int i = 0; i < anaLevel; i++)
				{
					final ImageView letter = new ImageView(ZeContext);
					ImageView guess = new ImageView(ZeContext);
					final String l = anaWord.substring(i, i + 1);
					Log.i(TAG, "LETTER : " + l);
					letter.setBackgroundResource(getIconIDFromLetter(l));
					letter.setEnabled(true);
					guess.setBackgroundResource(getIconIDFromLetter("?", false));
					anaWordLetters.add(letter);
					//
					TableRow rowWord = (TableRow) findViewById(R.id.rowWord);
					final TableRow rowGuess = (TableRow) findViewById(R.id.rowGuess);
					rowWord.addView(letter);
					rowGuess.addView(guess);
					//
					letter.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							letter.setBackgroundResource(getIconIDFromLetter(l, false));
							ImageView guess = (ImageView) rowGuess.getVirtualChildAt(anaColumn);
							guess.setBackgroundResource(getIconIDFromLetter(l, true));
							anaGuess = anaGuess + l;
							Log.i(TAG, "GUESS : " + anaGuess);
							letter.setEnabled(false);
							anaColumn++;
							if (anaColumn == anaLevel)
							{
								Log.i(TAG, "GUESS! : " + anaGuess);
								if (anaWords.contains(anaGuess))
								{
									anaScore++;
									anaNewWord();
								}
							}
						}
					});
				}
			}
		});
	}

	//***********************************************************************
	// showAnagrams()
	//***********************************************************************
	private void showAnagrams()
	{
		ImageView btnAnaRefresh = (ImageView) findViewById(R.id.btnAnaRefresh);
		// Hide the keyboard
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(btnAnaRefresh.getWindowToken(), 0);
		//
		anaNewWord();

		ImageView btnAnaClear = (ImageView) findViewById(R.id.btnAnaClear);
		btnAnaClear.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				TableRow rowWord = (TableRow) findViewById(R.id.rowWord);
				rowWord.removeAllViews();
				//
				final TableRow rowGuess = (TableRow) findViewById(R.id.rowGuess);
				rowGuess.removeAllViews();
				//
				anaColumn = 0;
				Log.i(TAG, anaWord);
				for (int i = 0; i < anaLevel; i++)
				{
					final ImageView letter = new ImageView(ZeContext);
					ImageView guess = new ImageView(ZeContext);
					final String l = anaWord.substring(i, i + 1);
					Log.i(TAG, "LETTER : " + l);
					letter.setBackgroundResource(getIconIDFromLetter(l));
					letter.setEnabled(true);
					guess.setBackgroundResource(getIconIDFromLetter("?", false));
					anaWordLetters.add(letter);
					rowWord.addView(letter);
					rowGuess.addView(guess);
					//
					letter.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							letter.setBackgroundResource(getIconIDFromLetter(l, false));
							ImageView guess = (ImageView) rowGuess.getVirtualChildAt(anaColumn);
							guess.setBackgroundResource(getIconIDFromLetter(l, true));
							letter.setEnabled(false);
							anaColumn++;
						}
					});
				}
			}
		});
	}

	//***********************************************************************
	// getIconIDFromLetter()
	//***********************************************************************
	private int getIconIDFromLetter(String letter)
	{
		int ic = R.mipmap.ic_dixio_letter_question;
		char c = letter.charAt(0);

		switch (c)
		{
			case '!':
				ic = getIconIDFromLetter("?", false);
				break;
			case '?':
				ic = getIconIDFromLetter("?", true);
				break;
			default:
				if (Character.isLowerCase(c))
				{
					ic = getIconIDFromLetter(letter, false);
				} else
				{
					ic = getIconIDFromLetter(letter, true);
				}
				break;
		}
		return ic;
	}

	//***********************************************************************
	// getIconIDFromLetter()
	//***********************************************************************
	private int getIconIDFromLetter(String letter, boolean enabled)
	{
		int ic = R.mipmap.ic_dixio_letter_question;

		if (enabled == true)
		{
			switch (letter.toUpperCase())
			{
				case "A":
					ic = R.mipmap.ic_dixio_letter_a;
					break;
				case "B":
					ic = R.mipmap.ic_dixio_letter_b;
					break;
				case "C":
					ic = R.mipmap.ic_dixio_letter_c;
					break;
				case "D":
					ic = R.mipmap.ic_dixio_letter_d;
					break;
				case "E":
					ic = R.mipmap.ic_dixio_letter_e;
					break;
				case "F":
					ic = R.mipmap.ic_dixio_letter_f;
					break;
				case "G":
					ic = R.mipmap.ic_dixio_letter_g;
					break;
				case "H":
					ic = R.mipmap.ic_dixio_letter_h;
					break;
				case "I":
					ic = R.mipmap.ic_dixio_letter_i;
					break;
				case "J":
					ic = R.mipmap.ic_dixio_letter_j;
					break;
				case "K":
					ic = R.mipmap.ic_dixio_letter_k;
					break;
				case "L":
					ic = R.mipmap.ic_dixio_letter_l;
					break;
				case "M":
					ic = R.mipmap.ic_dixio_letter_m;
					break;
				case "N":
					ic = R.mipmap.ic_dixio_letter_n;
					break;
				case "O":
					ic = R.mipmap.ic_dixio_letter_o;
					break;
				case "P":
					ic = R.mipmap.ic_dixio_letter_p;
					break;
				case "Q":
					ic = R.mipmap.ic_dixio_letter_q;
					break;
				case "R":
					ic = R.mipmap.ic_dixio_letter_r;
					break;
				case "S":
					ic = R.mipmap.ic_dixio_letter_s;
					break;
				case "T":
					ic = R.mipmap.ic_dixio_letter_t;
					break;
				case "U":
					ic = R.mipmap.ic_dixio_letter_u;
					break;
				case "V":
					ic = R.mipmap.ic_dixio_letter_v;
					break;
				case "W":
					ic = R.mipmap.ic_dixio_letter_w;
					break;
				case "X":
					ic = R.mipmap.ic_dixio_letter_x;
					break;
				case "Y":
					ic = R.mipmap.ic_dixio_letter_y;
					break;
				case "Z":
					ic = R.mipmap.ic_dixio_letter_z;
					break;
				default:
				case "?":
					ic = R.mipmap.ic_dixio_letter_question;
					break;
			}
		} else
		{
			switch (letter.toUpperCase())
			{
				case "A":
					ic = R.mipmap.ic_dixio_letter_disabled_a;
					break;
				case "B":
					ic = R.mipmap.ic_dixio_letter_disabled_b;
					break;
				case "C":
					ic = R.mipmap.ic_dixio_letter_disabled_c;
					break;
				case "D":
					ic = R.mipmap.ic_dixio_letter_disabled_d;
					break;
				case "E":
					ic = R.mipmap.ic_dixio_letter_disabled_e;
					break;
				case "F":
					ic = R.mipmap.ic_dixio_letter_disabled_f;
					break;
				case "G":
					ic = R.mipmap.ic_dixio_letter_disabled_g;
					break;
				case "H":
					ic = R.mipmap.ic_dixio_letter_disabled_h;
					break;
				case "I":
					ic = R.mipmap.ic_dixio_letter_disabled_i;
					break;
				case "J":
					ic = R.mipmap.ic_dixio_letter_disabled_j;
					break;
				case "K":
					ic = R.mipmap.ic_dixio_letter_disabled_k;
					break;
				case "L":
					ic = R.mipmap.ic_dixio_letter_disabled_l;
					break;
				case "M":
					ic = R.mipmap.ic_dixio_letter_disabled_m;
					break;
				case "N":
					ic = R.mipmap.ic_dixio_letter_disabled_n;
					break;
				case "O":
					ic = R.mipmap.ic_dixio_letter_disabled_o;
					break;
				case "P":
					ic = R.mipmap.ic_dixio_letter_disabled_p;
					break;
				case "Q":
					ic = R.mipmap.ic_dixio_letter_disabled_q;
					break;
				case "R":
					ic = R.mipmap.ic_dixio_letter_disabled_r;
					break;
				case "S":
					ic = R.mipmap.ic_dixio_letter_disabled_s;
					break;
				case "T":
					ic = R.mipmap.ic_dixio_letter_disabled_t;
					break;
				case "U":
					ic = R.mipmap.ic_dixio_letter_disabled_u;
					break;
				case "V":
					ic = R.mipmap.ic_dixio_letter_disabled_v;
					break;
				case "W":
					ic = R.mipmap.ic_dixio_letter_disabled_w;
					break;
				case "X":
					ic = R.mipmap.ic_dixio_letter_disabled_x;
					break;
				case "Y":
					ic = R.mipmap.ic_dixio_letter_disabled_y;
					break;
				case "Z":
					ic = R.mipmap.ic_dixio_letter_disabled_z;
					break;
				default:
				case "?":
					ic = R.mipmap.ic_dixio_letter_disabled_question;
					break;
			}
		}

		return ic;
	}

	//***********************************************************************
	// showInfo()
	//***********************************************************************
	private void showInfo()
	{
		String pattern = "yyyyMMdd.HHmmss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String buildString = simpleDateFormat.format(buildDate);
		//
		String html = "<html>";
		html += "<center><table>";
		html += "<tr><td rowspan=\"3\" align=\"center\" bgcolor=\"#b3c3dd\"><font color=\"#ffffff\" size=\"6\"><b>DIXIO</b></font></td><td>ODS consulting & gaming</td></tr>";
		html += "<tr><td>for Android systems</td></tr>";
		html += "<tr><td>Build " + buildString + "</td></tr>";
		html += "<tr><td colspan=\"2\" align=\"center\"><font color=\"#b3c3dd\">www.ozf.fr/#dixio</font></td></tr>";
		html += "<tr><td colspan=\"2\" align=\"center\">&nbsp;</td></tr>";
		html += "<tr><td colspan=\"2\" align=\"center\"><i>&copy; J.-P. Liguori 2017 - All rights reserved</i></td></tr>";
		html += "</table></center><br><br>";
		//
		html += "<center><font color=\"#b3c3dd\"><b>This application is not affiliated, endorsed or sponsored by SCRABBLE&reg; or Larousse.</b></font></center><br><br>";
		//
		html += "<p align=\"justify\"><i>SCRABBLE&reg; is a registered trademark. All intellectual property rights in and to the game are owned in the U.S.A and Canada by Hasbro Inc., and throughout the rest of the world by J.W. Spear & Sons Limited of Maidenhead, Berkshire, England, a subsidiary of Mattel Inc. Mattel and Spear are not affiliated with Hasbro.</i></p><br><br>";
		//
		html += "<center><i><b>Nombre et valeur des lettres</b></i></center><br>";
		html += "<center><table border=\"1\">";
		html += "<tr><th>Lettre</th><th>Nombre</th><th>Valeur</th><th>Lettre</th><th>Nombre</th><th>Valeur</th></tr>";
		html += "<tr><td align=\"center\"><b>A</b></td><td align=\"center\">9</td><td align=\"center\">1</td><td align=\"center\"><b>B</b></td><td align=\"center\">2</td><td align=\"center\">3</td></tr>";
		html += "<tr><td align=\"center\"><b>C</b></td><td align=\"center\">2</td><td align=\"center\">3</td><td align=\"center\"><b>D</b></td><td align=\"center\">3</td><td align=\"center\">2</td></tr>";
		html += "<tr><td align=\"center\"><b>E</b></td><td align=\"center\">15</td><td align=\"center\">1</td><td align=\"center\"><b>F</b></td><td align=\"center\">2</td><td align=\"center\">4</td></tr>";
		html += "<tr><td align=\"center\"><b>G</b></td><td align=\"center\">2</td><td align=\"center\">2</td><td align=\"center\"><b>H</b></td><td align=\"center\">2</td><td align=\"center\">4</td></tr>";
		html += "<tr><td align=\"center\"><b>I</b></td><td align=\"center\">8</td><td align=\"center\">1</td><td align=\"center\"><b>J</b></td><td align=\"center\">1</td><td align=\"center\">8</td></tr>";
		html += "<tr><td align=\"center\"><b>K</b></td><td align=\"center\">1</td><td align=\"center\">10</td><td align=\"center\"><b>L</b></td><td align=\"center\">5</td><td align=\"center\">1</td></tr>";
		html += "<tr><td align=\"center\"><b>M</b></td><td align=\"center\">3</td><td align=\"center\">2</td><td align=\"center\"><b>N</b></td><td align=\"center\">6</td><td align=\"center\">1</td></tr>";
		html += "<tr><td align=\"center\"><b>O</b></td><td align=\"center\">6</td><td align=\"center\">1</td><td align=\"center\"><b>P</b></td><td align=\"center\">2</td><td align=\"center\">3</td></tr>";
		html += "<tr><td align=\"center\"><b>Q</b></td><td align=\"center\">1</td><td align=\"center\">8</td><td align=\"center\"><b>R</b></td><td align=\"center\">6</td><td align=\"center\">1</td></tr>";
		html += "<tr><td align=\"center\"><b>S</b></td><td align=\"center\">6</td><td align=\"center\">1</td><td align=\"center\"><b>T</b></td><td align=\"center\">6</td><td align=\"center\">1</td></tr>";
		html += "<tr><td align=\"center\"><b>U</b></td><td align=\"center\">6</td><td align=\"center\">1</td><td align=\"center\"><b>V</b></td><td align=\"center\">2</td><td align=\"center\">4</td></tr>";
		html += "<tr><td align=\"center\"><b>W</b></td><td align=\"center\">1</td><td align=\"center\">10</td><td align=\"center\"><b>X</b></td><td align=\"center\">1</td><td align=\"center\">10</td></tr>";
		html += "<tr><td align=\"center\"><b>Y</b></td><td align=\"center\">1</td><td align=\"center\">10</td><td align=\"center\"><b>Z</b></td><td align=\"center\">1</td><td align=\"center\">10</td></tr>";
		html += "<tr><td align=\"center\"><b>&nbsp;</b></td><td align=\"center\">2</td><td align=\"center\">0</td><td align=\"center\" colspan=\"4\"><i>(joker)</i></td></tr>";
		html += "</table></center><br><br>";
		//
		html += "<center><i><b>Nombre de mots de l'ODS</b></i></center><br>";
		html += "<center><table border=\"1\">";
		html += "<tr><th>Version</th><th>Année</th><th>Mots</th></tr>";
		html += "<tr><td align=\"center\"><b>ODS2</b></td><td align=\"center\">1994</td><td align=\"center\">353 526</td></tr>";
		html += "<tr><td align=\"center\"><b>ODS3</b></td><td align=\"center\">1999</td><td align=\"center\">364 370</td></tr>";
		html += "<tr><td align=\"center\"><b>ODS4</b></td><td align=\"center\">2004</td><td align=\"center\">369 085</td></tr>";
		html += "<tr><td align=\"center\"><b>ODS5</b></td><td align=\"center\">2008</td><td align=\"center\">378 989</td></tr>";
		html += "<tr><td align=\"center\"><b>ODS6</b></td><td align=\"center\">2012</td><td align=\"center\">386 264</td></tr>";
		html += "<tr><td align=\"center\"><b>ODS7</b></td><td align=\"center\">2016</td><td align=\"center\">393 670</td></tr>";
		html += "</table></center><br><br>";
		//
		html += "<center><i><b>Glossaire des termes utilisés au SCRABBLE&reg;</b></i></center>";
		html += "<ul>";
		html += "<li><b>Benjamin</b> : prolongement d'un mot posé sur la grille de trois lettres par l'avant afin d'en former un autre, généralement pour atteindre une case « mot compte triple ». Par exemple avec VENIR, posé en H4 sur la grille, on pourrait envisager CONVENIR, PARVENIR ou SURVENIR.</li>";
		html += "<li><b>Beaufort</b> : coup technique qui consiste à tripler des lettres semi-chères pour obtenir plus qu’en doublant le mot.</li>";
		html += "<li><b>Blanchard</b> : scrabble marquant peu de points, car ne passant sur aucune case multiplicatrice.</li>";
		html += "<li><b>Caramel</b> : nom affectif donné aux jetons.</li>";
		html += "<li><b>Case</b> : un des 225 emplacements de la grille sur lesquels on place les lettres. Chaque case est identifiée par une référence alphanumérique.</li>";
		html += "<li><b>Cheminée</b> : collante dans laquelle les lettres sont placées « en sandwich » entre deux mots déjà posés sur la grille.</li>";
		html += "<li><b>Collante</b> : coup consistant à placer un mot le long d'un autre mot déjà posé sur la grille, formant alors de nouveaux mots, généralement de 2 lettres, dans l'autre sens.</li>";
		html += "<li><b>Grille</b> : le plateau de jeu de Scrabble, formé d'une grille de 225 (15 x 15) cases. Les colonnes sont référencées de 1 à 15 et les lignes de A à O. Elle peut être ouverte (les mots placés offrent de nombreuses possibilités de jeu) ou fermée (peu d'opportunités sont à la disposition du joueur).</li>";
		html += "<li><b>Joker</b> : la lettre blanche qui peut représenter n'importe quelle lettre.</li>";
		html += "<li><b>Legendre</b> : coup consistant à multiplier, d'une part, deux ou trois fois une lettre chère (ou semi-chère) par le placement sur une case bleu clair ou bleu foncé, et d'autre part à multiplier le mot par deux par le placement d'une des lettres sur une case rose.</li>";
		html += "<li><b>Lettre</b> : se dit des 26 lettres, ou des 102 jetons.</li>";
		html += "<li><b>Lettre blanche</b> : voir Joker.</li>";
		html += "<li><b>Lettre chère</b> : se dit des sept lettres qui ont une valeur supérieure à quatre points. Il s'agit de J et Q (8 points); K, W, X, Y et Z (10 points).</li>";
		html += "<li><b>Lettre semi-chère</b> : se dit d'une lettre qui a une valeur de deux, trois ou quatre points. En français, D, G, M (2 points), B, C, P (3 points), F, H, V (4 points).</li>";
		html += "<li><b>Maçonnerie</b> : coup formant plusieurs petits mots, le plus souvent en posant peu de lettres.</li>";
		html += "<li><b>Nonuple</b> : mot d'au moins huit lettres reliant deux cases « mot compte triple ». La valeur du mot est alors multipliée par neuf.</li>";
		html += "<li><b>ODS</b> : L'Officiel du Scrabble est le dictionnaire officiel du jeu de SCRABBLE&reg; francophone depuis le 1er janvier 1990. Il est édité par Larousse.</li>";
		html += "<li><b>Passer</b> : ne pas jouer de mot sur son tour. Le joueur marque alors zéro point.</li>";
		html += "<li><b>Pivot</b> : une lettre pivot est une lettre placée sur une case bleue à l'angle de deux nouveaux mots. Sa valeur est ainsi multipliée deux fois, verticalement et horizontalement.</li>";
		html += "<li><b>Quadruple</b> : mot d'au moins sept lettres reliant deux cases « mot compte double ». La valeur du mot est alors multipliée par quatre.</li>";
		html += "<li><b>Rallonge</b> : action de rallonger un mot en plaçant au moins une lettre au début ou à la fin du mot actuel. Par exemple REVENIR → PRÉVENIR, JOUER → JOUERA, PARLER → PARLERONT.</li>";
		html += "<li><b>Reliquat</b> : ensemble des lettres restant sur le chevalet ou dans le sac.</li>";
		html += "<li><b>Référence alphanumérique</b> : une référence pour décrire le placement d'un mot, qui consiste en une lettre de A à O et un nombre de 1 à 15. La case en haut à gauche a comme référence A1, et celle en bas à droite a comme référence O15. Pour indiquer un mot placé horizontalement, on commence par la lettre (exemple « C3 ») et pour indiquer un mot placé verticalement, on commence par le chiffre (exemple « 3C »).</li>";
		html += "<li><b>Scrabble</b> : coup consistant à former un mot en utilisant ses sept lettres, octroyant une bonification de 50 points.</li>";
		html += "<li><b>Sec</b> : se dit d'un Scrabble composé uniquement des sept lettres du tirage.</li>";
		html += "<li><b>Top</b> : mot dont le score est le plus élevé sur un coup.</li>";
		html += "</ul>";
		//
		html += "</html>";
		//
		WebView txtWordDefinition = (WebView) findViewById(R.id.webInfo);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(txtWordDefinition.getWindowToken(), 0);
		txtWordDefinition.loadData(html, "text/html; charset=utf-8", "utf-8");

	}

	//***********************************************************************
	// showWord()
	//***********************************************************************
	private void showWord(View v, String word)
	{
		runSearchOnline(v, word);
	}


	//***********************************************************************
	// MessageBox()
	//***********************************************************************
	private void MessageBox(String msg)
	{
		MessageBox("MessageBox", msg);
	}

	//***********************************************************************
	// MessageBox()
	//***********************************************************************
	private void MessageBox(String title, String msg)
	{
		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
		adb.setTitle(title);
		adb.setMessage(msg);
		adb.setPositiveButton("Ok", null);
		adb.show();
	}

	//***********************************************************************new ArrayList<String>()
	// id2img()
	//***********************************************************************
	private String id2img(int id)
	{
		return String.format("<img src=\"%s\"/>", id);
	}

	//***********************************************************************
	// id2img()
	//***********************************************************************
	private String id2img(String id)
	{
		return String.format("<img src=\"%s\"/>", id);
	}

	//***********************************************************************
	// displayHtml()
	//***********************************************************************
	private void displayHtml(int id, String html)
	{
		displayHtml((TextView) findViewById(id), html);
	}

	//***********************************************************************
	// displayHtml()
	//***********************************************************************
	private void displayHtml(TextView tv, String html)
	{
		tv.setText(Html.fromHtml(html,
				new Html.ImageGetter()
				{
					@Override
					public Drawable getDrawable(final String source)
					{
						Drawable d = null;
						try
						{
							d = getResources().getDrawable(Integer.parseInt(source));
							d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
						} catch (Resources.NotFoundException e)
						{
							Log.e("Log_Tag", "Image not found. Check the ID.", e);
						} catch (NumberFormatException e)
						{
							Log.e("Log_Tag", "Source string not a valid resource ID.", e);
						}

						return d;
					}
				}, null));
	}

	//***********************************************************************
	// getClassBuildTime()
	//***********************************************************************
	private Date getClassBuildTime()
	{
		// DON'T FORGET TO ADD THE FOLLOWING LINE
		// android.keepTimestampsInApk = true
		// IN gradle.properties FILE
		Date d = null;

		try
		{
			ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();
			d = new java.util.Date(time);
			zf.close();
		} catch (Exception e)
		{
		}
		return d;
	}

	//***********************************************************************
	// FindWords() asynchronous task
	//***********************************************************************
	private class FindWords extends AsyncTask<ODSLib, Void, ArrayList<String>>
	{
		public ArrayList<String> words = new ArrayList<String>();
		private ProgressDialog dialog;

		public FindWords(Activity c)
		{
			dialog = new ProgressDialog(c);
		}

		protected void onPreExecute()
		{
			dialog.setMessage(getString(R.string.str_searching));
			dialog.show();
		}

		protected ArrayList<String> doInBackground(ODSLib... odsLibs)
		{
			ArrayList<String> words = new ArrayList<String>();
			int count = odsLibs.length;
			for (int i = 0; i < count; i++)
			{
				words = odsLibs[i].findWords();
			}
			return words;
		}

		protected void onPostExecute(ArrayList<String> words)
		{
			TableLayout tbl = (TableLayout) findViewById(R.id.tblWords);

			while (tbl.getChildCount() > 1)
				tbl.removeView(tbl.getChildAt(tbl.getChildCount() - 1));

			this.words = words;
			ArrayList<String> sortedList = new ArrayList<String>();
			sortedList = ODSLib.sortWords(words, ODSLib.SORT_ALPHA);
			addWordsToTable(tbl, sortedList);

			TextView tvStats = (TextView) findViewById(R.id.txtWordsStats);
			int nbWords = odsLib.getWordsListSize(words);
			int maxLength = odsLib.getWordsListBestLength(words);
			int maxValue = odsLib.getWordsListBestValue(words);

			tvStats.setText(Integer.toString(nbWords) + " : " + Integer.toString(maxLength) + " / " + Integer.toString(maxValue));


			if (dialog.isShowing())
			{
				dialog.dismiss();
			}
		}
	}

	//***********************************************************************
	// FindWordOnline() asynchronous tasknew ImageView()
	//***********************************************************************
	private class FindWordOnline extends AsyncTask<ODSLib, Void, String>
	{
		public String definition = "";
		private ProgressDialog dialog;
		private String word = "";

		public FindWordOnline(Activity c)
		{
			dialog = new ProgressDialog(c);
		}

		protected void onPreExecute()
		{
			WebView txtDefinition = (WebView) findViewById(R.id.txtWordDefinition);
			txtDefinition.loadData("", "text/html; charset=utf-8", "utf-8");
			//
			TextView txtWord = (TextView) findViewById(R.id.txtSearchWord);
			this.word = txtWord.getText().toString();
			//
			dialog.setMessage(getString(R.string.str_searching));
			dialog.show();
		}

		protected String doInBackground(ODSLib... odsLibs)
		{
			String def = "<center><table>";
			int count = odsLibs.length;
			for (int i = 0; i < count; i++)
			{
				if (odsLibs[i].isValidWord(this.word))
				{
					def += "<tr><td><b>" + this.word + "</b></td><td bgcolor=\"#079153\" align=\"center\">Version " + odsLibs[i].getODSVersion() + "</td></tr>";
					def += "<tr><td colspan=\"2\" align=\"center\">Ce mot existe dans ce dictionnaire</td></tr>";
					def += "<tr><td>Lettres</td><td align=\"right\"><b>" + Integer.toString(this.word.length()) + "</b></td></tr>";
					def += "<tr><td>Points</td><td align=\"right\"><b>" + Integer.toString(odsLibs[i].getWordValue(this.word)) + "</b></td></tr>";
					def += "</table></center><br><br>";
					//
					def += "<i>Définition fournie par <b>" + odsLibs[i].getDicoVersion(prefOnlineDico) + "</b> : </i><br><br>";
					def += odsLibs[i].getDefinition(prefOnlineDico, this.word);
					def += "<p><center><i>&copy; J.-P. Liguori 2017 - All rights reserved</i></center></p>";
					//
				} else
				{
					def += "<tr><td><b>" + this.word + "</b></td><td bgcolor=\"#d3220e\" align=\"center\">Version " + odsLibs[i].getODSVersion() + "</td></tr>";
					def += "<tr><td colspan=\"2\" align=\"center\">Ce mot n'existe pas dans ce dictionnaire</td></tr>";
					def += "</table></center><br><br>";
				}
			}
			Log.i(TAG, def);
			return def;
		}

		protected void onPostExecute(String definition)
		{
			this.definition = definition;
			WebView txtWordDefinition = (WebView) findViewById(R.id.txtWordDefinition);
			txtWordDefinition.loadData(definition, "text/html; charset=utf-8", "utf-8");

			if (dialog.isShowing())
			{
				dialog.dismiss();
			}
		}
	}
	//***********************************************************************
	// FindWords() asynchronous task
	//***********************************************************************
	private class GenerateNewAnagram extends AsyncTask<ODSLib, Void, ArrayList<String>>
	{
		public ArrayList<String> words = new ArrayList<String>();
		private ProgressDialog dialog;

		public GenerateNewAnagram(Activity c)
		{
			dialog = new ProgressDialog(c);
		}

		protected void onPreExecute()
		{
			dialog.setMessage(getString(R.string.str_searching));
			dialog.show();
		}

		protected ArrayList<String> doInBackground(ODSLib... odsLibs)
		{
			ArrayList<String> words = new ArrayList<String>();
			int count = odsLibs.length;
			for (int i = 0; i < count; i++)
			{
				words = odsLibs[i].findWords();
			}
			return words;
		}

		protected void onPostExecute(ArrayList<String> words)
		{
			TableLayout tbl = (TableLayout) findViewById(R.id.tblWords);

			while (tbl.getChildCount() > 1)
				tbl.removeView(tbl.getChildAt(tbl.getChildCount() - 1));

			this.words = words;
			ArrayList<String> sortedList = new ArrayList<String>();
			sortedList = ODSLib.sortWords(words, ODSLib.SORT_ALPHA);
			addWordsToTable(tbl, sortedList);

			TextView tvStats = (TextView) findViewById(R.id.txtWordsStats);
			int nbWords = odsLib.getWordsListSize(words);
			int maxLength = odsLib.getWordsListBestLength(words);
			int maxValue = odsLib.getWordsListBestValue(words);

			tvStats.setText(Integer.toString(nbWords) + " : " + Integer.toString(maxLength) + " / " + Integer.toString(maxValue));


			if (dialog.isShowing())
			{
				dialog.dismiss();
			}
		}
	}
}



