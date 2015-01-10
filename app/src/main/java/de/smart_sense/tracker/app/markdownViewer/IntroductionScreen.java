package de.smart_sense.tracker.app.markdownViewer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import de.smart_sense.tracker.app.R;

public class IntroductionScreen extends MarkdownViewerActivity {

    @Override
    protected String markdownFile() {
        return "introduction";
    }

}
