package ess.imu_logger.app.markdownViewer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import ess.imu_logger.app.R;

public class AboutScreen extends MarkdownViewerActivity {

    @Override
    protected String markdownFile() {
        return "about";
    }

}
