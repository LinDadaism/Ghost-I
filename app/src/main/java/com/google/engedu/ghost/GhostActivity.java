/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.ghost;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.Random;


public class GhostActivity extends AppCompatActivity {
    private static final String COMPUTER_TURN = "Computer's turn";
    private static final String USER_TURN = "Your turn";
    private GhostDictionary dictionary;
    private boolean userTurn = false;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            dictionary = new SimpleDictionary(inputStream);
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }

        // Capture Challenge button from layout
        Button challenge = (Button) findViewById(R.id.Challenge);
        // Register the onClick listener with the implementation of OnClickListener
        challenge.setOnClickListener(mCorkyListener);

        Button restart = (Button) findViewById(R.id.Restart);
        restart.setOnClickListener(mCorkyListener2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ghost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handler for the "Challenge" button.
     */
    private View.OnClickListener mCorkyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView label = (TextView) findViewById(R.id.gameStatus);
            TextView text = (TextView) findViewById(R.id.ghostText);
            String word = text.getText().toString();
            if (dictionary.isWord(word) && word.length() >= 4) {
                label.setText("It's a complete word. User wins!");
            } else if (dictionary.getAnyWordStartingWith(word) != null) {
                label.setText("Computer wins :(");
                text.setText(dictionary.getAnyWordStartingWith(word));
            } else {
                label.setText("No word can be formed. User wins!");
            }
            return;
        }
    };

    /**
     * Handler for the "Reset" button.
     * Randomly determines whether the game starts with a user turn or a computer turn.
     * @param view
     * @return true
     */
    private View.OnClickListener mCorkyListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onStart(null);
        }
    };

    public boolean onStart(View view) {
        userTurn = random.nextBoolean();
        TextView text = (TextView) findViewById(R.id.ghostText);
        text.setText("");
        TextView label = (TextView) findViewById(R.id.gameStatus);
        if (userTurn) {
            label.setText(USER_TURN);
        } else {
            label.setText(COMPUTER_TURN);
            computerTurn();
        }
        return true;
    }

    private void computerTurn() {
        TextView label = (TextView) findViewById(R.id.gameStatus);
        TextView word = (TextView) findViewById(R.id.ghostText);
        String current = word.getText().toString();userTurn = true;
        label.setText(USER_TURN);

        if (dictionary.isWord(current) && current.length() >= 4) {
            label.setText("Computer wins :(");
            return;
        }

        String longerWord = dictionary.getAnyWordStartingWith(current);
        if (longerWord == null) {
            label.setText("You can't bluff this computer!");
            return;
        } else {
            word.setText(longerWord.substring(0, current.length()+1));
            userTurn = true;
            label.setText(USER_TURN);
        }
    }

    /**
     * Handler for user key presses.
     * @param keyCode
     * @param event
     * @return whether the key stroke was handled.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        TextView word = (TextView) findViewById(R.id.ghostText);
        String current = word.getText().toString();
        if (keyCode > 28 && keyCode < 55) {
            current += (char) event.getUnicodeChar();
            word.setText(current);
        }
        computerTurn();
        return super.onKeyUp(keyCode, event);
    }
}
