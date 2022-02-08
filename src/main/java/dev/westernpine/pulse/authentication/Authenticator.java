package dev.westernpine.pulse.authentication;

import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.Controller;
import dev.westernpine.pulse.controller.settings.setting.Setting;
import dev.westernpine.pulse.properties.IdentityProperties;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Authenticator {

    public static boolean isBotAdmin(User user) {
        List<String> admins = new ArrayList<>();
        String adminsString = Pulse.identityProperties.get(IdentityProperties.ADMINS);
        String[] adminsSplit = adminsString.split(", ");
        if (adminsString.contains(", ")) {
            admins.addAll(Arrays.asList(adminsSplit));
        } else {
            admins.add(adminsString);
        }
        return admins.contains(user.getId());
    }

    public static boolean hasPermission(Member member, Permission permission) {
        if (member.isOwner())
            return true;
        for (Role role : member.getRoles()) {
            if (role.getPermissions().contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRole(Member member, String roleId) {
        for (Role role : member.getRoles()) {
            if (role.getId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(Member member) {
        return hasPermission(member, Permission.ADMINISTRATOR);
    }

    public static boolean isDj(Member member, Controller controller) {
        if(isBotAdmin(member.getUser()))
            return true;

        if(isAdmin(member))
            return true;

        if(!controller.getSettings().get(Setting.DJ_MODE).toBoolean())
            return true;

        String djRole = controller.getSettings().get(Setting.DJ_ROLE).toString();

        return hasRole(member, controller.getSettings().get(Setting.DJ_ROLE).toString());
    }

}
