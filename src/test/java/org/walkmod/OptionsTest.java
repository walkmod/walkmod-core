package org.walkmod;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Tests for {@link Options} and {@link OptionsBuilder}
 */
public class OptionsTest {

    @Test
    public void options_initialize_default_values() {
        Options options = new Options();
        
        assertThat(options.isOffline(), is(false));
        assertThat(options.isVerbose(), is(true));
        assertThat(options.isPrintErrors(), is(false));
        assertThat(options.isThrowException(), is(false));
        assertThat(options.getExcludes(), is(nullValue()));
        assertThat(options.getIncludes(), is(nullValue()));
       
    }

    @Test
    public void options_does_can_initialize_values_from_map() {
        Map<String,Object> myOptions = new HashMap<String, Object>();
        myOptions.put(Options.OFFLINE, true);
        myOptions.put(Options.THROW_EXCEPTION, true);
        myOptions.put(Options.INCLUDES, Arrays.asList("one/path", "two/path", "three/path"));

        Options _options = new Options(myOptions);
        Map<String,Object> options = _options.asMap();
        assertThat(options.values().size(), is(5)); //default values(4) + includes
        assertThat(_options.getIncludes().size(), is(3));
        assertThat(_options.isOffline(), is(true));
        assertThat(_options.isThrowException(), is(true));
        assertThat(_options.getIncludes(), contains("one/path", "two/path", "three/path"));
    }

    @Test
    public void offline_option_setter_works() {
        Options options = new Options();
       
        options.setOffline(true);
        assertThat(options.isOffline(), is(true));

        options.setOffline(false);
        assertThat(options.isOffline(), is(false));
    }

    @Test
    public void verbose_option_setter_works() {
        Options options = new Options();
      
        options.setVerbose(true);
        assertThat(options.isVerbose(), is(true));

        options.setVerbose(false);
        assertThat(options.isVerbose(), is(false));
    }

    @Test
    public void printErrors_option_setter_works() {
        Options options = new Options();
       
        options.setPrintErrors(true);
        assertThat(options.isPrintErrors(), is(true));

        options.setPrintErrors(false);
        assertThat(options.isPrintErrors(), is(false));
    }

    @Test
    public void throwsException_option_setter_works() {
        Options options = new Options();
        
        options.setThrowException(true);
        assertThat(options.isThrowException(), is(true));

        options.setThrowException(false);
        assertThat(options.isThrowException(), is(false));
    }

    @Test
    public void include_option_setter_multiple_works() {
        Options options = new Options();
        Object previousValue = options.getIncludes();
        assertThat(previousValue, is(nullValue()));

        options.setIncludes("one", "two", "path/three");
        assertThat(options.getIncludes().size(), is(3));
        assertThat(options.getIncludes(), contains("one", "two", "path/three"));
    }

    @Test
    public void include_option_setter_one_by_one_works() {
        Options options = new Options();
        Object previousValue = options.getIncludes();
        assertThat(previousValue, is(nullValue()));

        options.setIncludes("one");
        options.setIncludes("two");
        options.setIncludes( "path/three");
        assertThat(options.getIncludes().size(), is(3));
        assertThat(options.getIncludes(), contains("one", "two", "path/three"));
    }

    @Test
    public void exclude_option_setter_multiple_works() {
        Options options = new Options();
        Object previousValue = options.getExcludes();
        assertThat(previousValue, is(nullValue()));

        options.setExcludes("one", "two", "path/three");
        assertThat(options.getExcludes().size(), is(3));
        assertThat(options.getExcludes(), contains("one", "two", "path/three"));
    }

    @Test
    public void exclude_option_setter_one_by_one_works() {
        Options options = new Options();
        Object previousValue = options.getExcludes();
        assertThat(previousValue, is(nullValue()));

        options.setExcludes("one");
        options.setExcludes("two");
        options.setExcludes("path/three");
        assertThat(options.getExcludes().size(), is(3));
        assertThat(options.getExcludes(), contains("one", "two", "path/three"));
    }

    /**
     * OptionsBuilder tests start here
     */
    @Test
    public void optionsBuilder_initializes_options_values() {

        OptionsBuilder builder = OptionsBuilder.options();
        Options options = builder.build();

        assertThat(options.isOffline(), is(false));
        assertThat(options.isVerbose(), is(true));
        assertThat(options.isPrintErrors(), is(false));
        assertThat(options.isThrowException(), is(false));

        assertThat(options.getIncludes(), is(nullValue()));
        assertThat(options.getExcludes(), is(nullValue()));
    }

    @Test
    public void optionsBuilder_initializes_options_values_with_a_map() {

        Map<String, Object> myOptions = new HashMap<String, Object>();
        myOptions.put(Options.OFFLINE, true);
        myOptions.put(Options.VERBOSE, false);

        OptionsBuilder builder = OptionsBuilder.options(myOptions);
        Options options = builder.build();

        // Values from myOptions map
        assertThat(options.isOffline(), is(true));
        assertThat(options.isVerbose(), is(false));
        // Default values are also set
        assertThat(options.isPrintErrors(), is(false));
        assertThat(options.isThrowException(), is(false));

        assertThat(options.getIncludes(), is(nullValue()));
        assertThat(options.getExcludes(), is(nullValue()));

        // Added test for PRINT_ERRORS and THROW_EXCEPTION just for coverage
        myOptions = new HashMap<String, Object>();
        myOptions.put(Options.PRINT_ERRORS, true);
        myOptions.put(Options.THROW_EXCEPTION, true);

        builder = OptionsBuilder.options(myOptions);
        options = builder.build();

        // Values from myOptions map
        assertThat(options.isOffline(), is(false));
        assertThat(options.isVerbose(), is(true));
        // Default values are also set
        assertThat(options.isPrintErrors(), is(true));
        assertThat(options.isThrowException(), is(true));

        assertThat(options.getIncludes(), is(nullValue()));
        assertThat(options.getExcludes(), is(nullValue()));
    }

    @Test
    public void optionsBuilder_chain_configuration() {

        OptionsBuilder builder = OptionsBuilder.options().offline(true).verbose(false).includes("one", "two");
        builder.throwException(true);
        Options options = builder.build();

        assertThat(options.isOffline(), is(true));
        assertThat(options.isVerbose(), is(false));
        assertThat(options.isPrintErrors(), is(false));
        assertThat(options.isThrowException(), is(true));

        assertThat(options.getIncludes(), contains("one","two"));
        assertThat(options.getExcludes(), is(nullValue()));


        // Added test for printErrors and exclude just for coverage
        builder = OptionsBuilder.options().printErrors(true).excludes("three", "four");
        options = builder.build();

        assertThat(options.isOffline(), is(false));
        assertThat(options.isVerbose(), is(true));
        assertThat(options.isPrintErrors(), is(true));
        assertThat(options.isThrowException(), is(false));

        assertThat(options.getIncludes(), is(nullValue()));
        assertThat(options.getExcludes(), contains("three","four"));
    }

}
