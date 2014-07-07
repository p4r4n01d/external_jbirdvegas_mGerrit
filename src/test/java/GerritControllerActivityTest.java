package test.java;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
public class GerritControllerActivityTest {

    @Test
    public void shouldFail() {
        assertTrue(false);
    }

    // onStart
    //  - Registers for the eventbus

    // onStop
    //  - Unregisters for the eventbus

    // onCreate should
    //  - Set the theme
    //  - Set the current Gerrit instance if one was provided (Not currently used)
    //  - Inflate R.layout.main
    //  - Sets whether running in tablet mode

    // onPrepareOptionsMenu should
    //  - Calls hideChangelogOption

    // onCreateOptionsMenu should
    //  - Inflate R.menu.gerrit_instances_menu

    // onOptionsItemSelected should
    //  When save:
    //      - Starts PrefsActivity
    //  When help
    //      - Shows help dialog
    //  When refresh
    //      - calls refreshTabs
    //  When change Gerrit
    //      - Starts GerritSwitcher
    //  When changelog
    //      - Starts the AOKP changelog
    //  When search
    //      - Toggles visibility

    // onGerritChanged should
    //  - Show a toast
    //  - Call hideChangelogOption
    //  - Call refreshTabs

    // refreshTabs should
    //  - Delegate work to change list

    // onResume should
    //  - Call onGerritChanged if the Gerrit changed
    //  - Apply the theme if it changed

    // hideChangelogOption
    //  - Hides the changelog menu option when not AOKP
    //  - Shows the changelog menu option when AOKP

    // getChangeList

    // getChangeDetail

    // showHelpDialog should
    //  - Inflate and show R.layout.dialog_help
    //  - Have a button R.string.gerrit_help
    //  - Opens browser to http://gerrit.aokp.co/Documentation/index.html when AOKP Gerrit is selected
    //  - Opens browser to http://gerrit.cyanogenmod.org/Documentation/index.html when CM Gerrit is selected


    // onEventMainThread -> GerritChanged
    //  - Should call onGerritChanged with the new Gerrit

    // onEventMainThread -> NewChangeSelected should
    //  - Set the selected change
    //  - Change the change details panel to show the new change


}

