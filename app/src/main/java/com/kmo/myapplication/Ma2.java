//package com.kmo.myapplication;
//
//import android.app.Activity;
//import android.os.Bundle;
//
//import androidx.annotation.Nullable;
//
//import com.example.piccut.R;
//
//public class Ma2 extends Activity {
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.topbar_2023_1);
//        val title1 = findViewById<Placeholder>(R.id.title_name_placeholder_1)
//                val title2 = findViewById<Placeholder>(R.id.title_name_placeholder_2)
//                val menubar1 = findViewById<Placeholder>(R.id.menubar_placeholder_1)
//                val menubar2 = findViewById<Placeholder>(R.id.menubar_placeholder_2)
//
//
//                val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
//                radioGroup.setOnCheckedChangeListener({ group, checkedId ->
//                        TransitionManager.beginDelayedTransition(findViewById(R.id.container))
//                        when (checkedId) {
//                        R.id.radio_1 -> {
//            title1.setContentId(R.id.title)
//            menubar1.setContentId(R.id.menubar)
//            title2.setContentId(-1)
//            menubar2.setContentId(-1)
//            title2.emptyVisibility = View.GONE
//            menubar2.emptyVisibility = View.GONE
//
//        }
//        R.id.radio_2 -> {
//            title2.setContentId(R.id.title)
//            menubar2.setContentId(R.id.menubar)
//            title1.emptyVisibility = View.GONE
//            menubar1.emptyVisibility = View.GONE
//            title1.setContentId(-1)
//            menubar1.setContentId(-1)
//        }
//
//        R.id.radio_3 -> {
//            title1.setContentId(R.id.title)
//            menubar2.setContentId(R.id.menubar)
//            title2.emptyVisibility = View.GONE
//            menubar1.emptyVisibility = View.GONE
//            title2.setContentId(-1)
//            menubar1.setContentId(-1)
//        }
//            }
//        })
//
//        findViewById<RadioButton>(R.id.radio_1).setChecked(true)
//
//    }
//}
