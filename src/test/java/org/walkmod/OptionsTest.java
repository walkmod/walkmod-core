package org.walkmod;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link Options} and {@link OptionsBuilder}
 */
public class OptionsTest {

    @Test
    public void options_does_not_initialize_values() {
        Options _options = new Options();
        Map<String,Object> options = _options.asMap();
        assertThat(options.values(), is(empty()));
    }


    @Test
    public void offline_option_setter_works() {
        Options options = new Options();
        Boolean value = (Boolean)options.asMap().get(Options.OFFLINE);
        assertThat(value, is(nullValue()));

        options.setOffline(true);
        assertThat((Boolean) options.asMap().get(Options.OFFLINE), is(true));

        options.setOffline(false);
        assertThat((Boolean) options.asMap().get(Options.OFFLINE), is(false));
    }

    @Test
    public void verbose_option_setter_works() {
        Options options = new Options();
        Boolean value = (Boolean)options.asMap().get(Options.VERBOSE);
        assertThat(value, is(nullValue()));

        options.setVerbose(true);
        assertThat((Boolean) options.asMap().get(Options.VERBOSE), is(true));

        options.setVerbose(false);
        assertThat((Boolean) options.asMap().get(Options.VERBOSE), is(false));
    }

    @Test
    public void printErrors_option_setter_works() {
        Options options = new Options();
        Boolean value = (Boolean)options.asMap().get(Options.PRINT_ERRORS);
        assertThat(value, is(nullValue()));

        options.setPrintErrors(true);
        assertThat((Boolean) options.asMap().get(Options.PRINT_ERRORS), is(true));

        options.setPrintErrors(false);
        assertThat((Boolean) options.asMap().get(Options.PRINT_ERRORS), is(false));
    }

    @Test
    public void throwsException_option_setter_works() {
        Options options = new Options();
        Boolean value = (Boolean)options.asMap().get(Options.THROW_EXCEPTION);
        assertThat(value, is(nullValue()));

        options.setThrowException(true);
        assertThat((Boolean) options.asMap().get(Options.THROW_EXCEPTION), is(true));

        options.setThrowException(false);
        assertThat((Boolean) options.asMap().get(Options.THROW_EXCEPTION), is(false));
    }

    @Test
    public void include_option_setter_multiple_works() {
        Options options = new Options();
        Object previousValue = options.asMap().get(Options.INCLUDES);
        assertThat(previousValue, is(nullValue()));

        options.setIncludes("one", "two", "path/three");
        assertThat(((List<String>) options.asMap().get(Options.INCLUDES)).size(), is(3));
        assertThat((List<String>) options.asMap().get(Options.INCLUDES), contains("one", "two", "path/three"));
    }

    @Test
    public void include_option_setter_one_by_one_works() {
        Options options = new Options();
        Object previousValue = options.asMap().get(Options.INCLUDES);
        assertThat(previousValue, is(nullValue()));

        options.setIncludes("one");
        options.setIncludes("two");
        options.setIncludes( "path/three");
        assertThat(((List<String>) options.asMap().get(Options.INCLUDES)).size(), is(3));
        assertThat((List<String>) options.asMap().get(Options.INCLUDES), contains("one", "two", "path/three"));
    }

    @Test
    public void exclude_option_setter_multiple_works() {
        Options options = new Options();
        Object previousValue = options.asMap().get(Options.EXCLUDES);
        assertThat(previousValue, is(nullValue()));

        options.setExcludes("one", "two", "path/three");
        assertThat(((List<String>) options.asMap().get(Options.EXCLUDES)).size(), is(3));
        assertThat((List<String>) options.asMap().get(Options.EXCLUDES), contains("one", "two", "path/three"));
    }

    @Test
    public void exclude_option_setter_one_by_one_works() {
        Options options = new Options();
        Object previousValue = options.asMap().get(Options.EXCLUDES);
        assertThat(previousValue, is(nullValue()));

        options.setExcludes("one");
        options.setExcludes("two");
        options.setExcludes("path/three");
        assertThat(((List<String>) options.asMap().get(Options.EXCLUDES)).size(), is(3));
        assertThat((List<String>) options.asMap().get(Options.EXCLUDES), contains("one", "two", "path/three"));
    }

    /**
     *
     * */
    @Test
    public void optionsBuilder_initializes_options_values() {

        OptionsBuilder builder = OptionsBuilder.options();
        Map<String,Object> options = builder.asMap();

        assertThat((Boolean) options.get(Options.OFFLINE), is(false));
        assertThat((Boolean) options.get(Options.VERBOSE), is(true));
        assertThat((Boolean) options.get(Options.PRINT_ERRORS), is(false));
        assertThat((Boolean) options.get(Options.THROW_EXCEPTION), is(false));

        assertThat(options.get(Options.INCLUDES), is(nullValue()));
        assertThat(options.get(Options.EXCLUDES), is(nullValue()));
    }

    @Test
    public void optionsBuilder_chain_configuration() {

        OptionsBuilder builder = OptionsBuilder.options().offline(true).verbose(false).includes("one", "two");
        builder.throwException(true);
        Map<String, Object> options = builder.asMap();

        assertThat((Boolean) options.get(Options.OFFLINE), is(true));
        assertThat((Boolean) options.get(Options.VERBOSE), is(false));
        assertThat((Boolean) options.get(Options.PRINT_ERRORS), is(false));
        assertThat((Boolean) options.get(Options.THROW_EXCEPTION), is(true));

        assertThat((List<String>) options.get(Options.INCLUDES), contains("one","two"));
        assertThat(options.get(Options.EXCLUDES), is(nullValue()));


        // Added test for printErrors and exclude just for coverage
        OptionsBuilder builder2 = OptionsBuilder.options().printErrors(true).excludes("three", "four");
        Map<String, Object> options2 = builder2.asMap();

        assertThat((Boolean) options2.get(Options.OFFLINE), is(false));
        assertThat((Boolean) options2.get(Options.VERBOSE), is(true));
        assertThat((Boolean) options2.get(Options.PRINT_ERRORS), is(true));
        assertThat((Boolean) options2.get(Options.THROW_EXCEPTION), is(false));

        assertThat(options2.get(Options.INCLUDES), is(nullValue()));
        assertThat((List<String>) options2.get(Options.EXCLUDES), contains("three","four"));
    }

}
