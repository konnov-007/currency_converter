package com.example.ilya.currencyconverter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    ArrayAdapter<CharSequence> adapter;
    Spinner spinner1;
    TextView exchangeRateOutput;
    TextView dateOutput;
    EditText inputedText;
    TextView convertedCurrency;
    StringBuilder outputString = new StringBuilder();
    String dateString;
    ProgressBar progressBar;
    double exRateF = 0;
    private static final String TAG = "myLogs";
    private enum Currency {USD, RUB};
    Currency selectedCurrency = Currency.USD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dateOutput = (TextView) findViewById(R.id.date);
        exchangeRateOutput = (TextView) findViewById(R.id.exchange_rate_usd_rub);
        inputedText = (EditText) findViewById(R.id.inputEditText);
        convertedCurrency = (TextView) findViewById(R.id.convertedCurrencyTextView);
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getSelectedItemPosition() == 0)
                    selectedCurrency = Currency.USD;
                    //Toast.makeText(getBaseContext(), "USD", Toast.LENGTH_LONG).show();
                if(adapterView.getSelectedItemPosition() == 1)
                    selectedCurrency = Currency.RUB;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    public void button_clicked(View v) throws IOException {
        progressBar.setVisibility(View.VISIBLE);
        getWebsite();
    }


    private void getWebsite() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int c;
                URL hp = null;
                try {
                    hp = new URL("https://finance.google.com/finance?q=usdrub");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                URLConnection hpCon = null;
                try {
                    assert hp != null;
                    hpCon = hp.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //get date
                long d = 0;
                if (hpCon != null) {
                    d = hpCon.getDate();
                }
                if(d==0)
                    Log.d(TAG, "No date information");
                else
                    dateString = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(d)));
                    //Log.d(TAG, String.valueOf(new Date(d)));
                /*
                //get content type
                assert hpCon != null;
                Log.d(TAG, "Conten\t-Type: " + hpCon.getContentType());

                //get expiration date
                d = hpCon.getExpiration();
                if(d==0)
                    Log.d(TAG, "No expiration information");

                else
                    Log.d(TAG, "Expires: " + new Date(d));


                //get last-modified date
                d = hpCon.getLastModified();
                if(d==0)
                    Log.d(TAG, "No last-modified information");

                else
                    Log.d(TAG, "Last-Modified: " + new Date(d));
                */
                //get content length
                assert hpCon != null;
                long len = hpCon.getContentLength();
                if(len == -1)
                    Log.d(TAG, "Content length unavailable");

                else
                    Log.d(TAG, "Content-Length: " + len);

                if(len!= 0){
                    Log.d(TAG, "=== Content ===");
                    try {
                        InputStream input = hpCon.getInputStream();
                        int index = 0;
                        while (((c = input.read()) != -1)) {
                            //Log.d(TAG, String.valueOf((char) c));
                            if(index>87000) {
                                //Log.d(TAG, String.valueOf((char) c) + "  " + String.valueOf(index));
                                outputString.append((char) c);
                                if (outputString.indexOf(" RUB</span>") != -1)
                                    break;
                            }
                            index++;
                        }
                        Log.d(TAG, String.valueOf(index));
                        input.close();

                        System.out.println("\n\n\n\n\n");
                        outputString = new StringBuilder(outputString.substring(outputString.indexOf("1 USD ="), outputString.indexOf(" RUB</span>") + 4));
                        outputString = new StringBuilder("1 USD = " + outputString.substring(outputString.indexOf("bld>") + 4, outputString.indexOf("RUB") + 3));
                        exRateF = Double.parseDouble(outputString.substring(outputString.indexOf("bld>") + 9, outputString.indexOf("RUB") -1));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dateOutput.setText(dateString);
                                exchangeRateOutput.setText(outputString);
                                outputString = new StringBuilder();
                                converter();
                                progressBar.setVisibility(View.GONE);
                                Thread.interrupted();
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    exchangeRateOutput.setText(R.string.network_error);
                    Log.d(TAG, "No content available");
                }
            }
        }).start();
    }


    private void converter(){
        double inputedDouble = 0;
        try {
            inputedDouble = Double.parseDouble(inputedText.getText().toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        if(selectedCurrency == Currency.USD && exRateF != 0 && inputedDouble != 0)
            convertedCurrency.setText(String.valueOf(exRateF*inputedDouble) + " RUB");
        if(selectedCurrency == Currency.RUB && exRateF != 0 && inputedDouble != 0)
            convertedCurrency.setText(String.valueOf((1/exRateF)*inputedDouble) + " USD");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        clear();
        return super.onOptionsItemSelected(item);
    }



    private void clear(){
        exchangeRateOutput.setText("");
        dateOutput.setText("");
        inputedText.setText("");
        convertedCurrency.setText("");
        dateString = null;
        exRateF = 0;
    }

}