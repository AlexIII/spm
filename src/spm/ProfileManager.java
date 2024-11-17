package spm;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collection;

public class ProfileManager {
    public static class Profile {
        public String name;
        public String path;
        public Profile(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }

    public static final String FILE_NAME_PREFIX = "spmdb.";
    private static final Pattern PROFILE_PATTERN = Pattern.compile("spmdb(\\..+)?\\.xml");

    public static List<Profile> find(String basePath) {
        File dir = new File(basePath);
        if (!dir.isDirectory()) {
            return new ArrayList<>();
        }

        return Arrays.stream(dir.listFiles())
            .filter(file -> PROFILE_PATTERN.matcher(file.getName()).matches() && file.isFile())
            .map(File::getAbsolutePath)
            .map(file -> new Profile(DataTable.getProfileName(file), file))
            .sorted((a, b) -> a.name.compareTo(b.name))
            .collect(Collectors.toList());
    }

    public static List<Profile> sort(Collection<Profile> profiles) {
        return profiles.stream()
            .sorted((a, b) -> a.name.compareTo(b.name))
            .collect(Collectors.toList());
    }

    public static List<Profile> addAndSort(List<Profile> profiles, String profileName, String profilePath) {
        profiles.add(new Profile(profileName, profilePath));
        return sort(profiles);
    } 
}
