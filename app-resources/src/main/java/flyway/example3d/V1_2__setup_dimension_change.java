package flyway.example3d;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.AppsetupHelper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class V1_2__setup_dimension_change extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_2__setup_dimension_change.class);
    private static final String BUNDLE_NAME = "dimension-change";
    private static final String APPLICATION_3D_NAME = "geoportal-3d";
    private static final String APPLICATION_2D_NAME = "geoportal";

    public void migrate(Context context) throws SQLException {
        Connection conn = context.getConnection();
        // Set 3D view's uuid for 2D default and user appsetups
        List<Long> viewIds2d = AppsetupHelper.getSetupsForUserAndDefaultType(conn, APPLICATION_2D_NAME);
        String uuid3d = AppsetupHelper.getUuidForDefaultSetup(conn, APPLICATION_3D_NAME);
        setBundleForViews(conn, viewIds2d, uuid3d);

        // Set default 2D view's uuid for 3D default appsetup
        List<Long> viewIds3d = AppsetupHelper.getSetupsForUserAndDefaultType(conn, APPLICATION_3D_NAME);
        String uuid2d =  AppsetupHelper.getUuidForDefaultSetup( conn, APPLICATION_2D_NAME);
        setBundleForViews(conn, viewIds3d, uuid2d);
    }
    private void setBundleForViews (Connection conn, List<Long> ids, String uuid) throws SQLException {
        String conf = JSONHelper.getStringFromJSON(
                JSONHelper.createJSONObject("uuid", uuid)
                ,"{}");
        for (Long id : ids) {
            if (!AppsetupHelper.appContainsBundle(conn, id, BUNDLE_NAME)) {
                AppsetupHelper.addBundleToApp(conn, id, BUNDLE_NAME);
            }
            Bundle bundle = AppsetupHelper.getAppBundle(conn, id, BUNDLE_NAME);
            bundle.setConfig(conf);
            AppsetupHelper.updateAppBundle(conn, id, bundle);
        }
        LOG.info("Set view uuid:", uuid, "to dimension change bundle config. Updated views:", ids);
    }
}
