package de.hasait.teleport.ui.puif;

import de.hasait.common.ui.puif.AbstractToOnePuiFactory;
import de.hasait.teleport.domain.LocationPO;
import de.hasait.teleport.domain.LocationRepository;
import org.springframework.stereotype.Service;

@Service
public class LocationPuif extends AbstractToOnePuiFactory<LocationPO, LocationRepository, Void> {

    public LocationPuif(LocationRepository repository) {
        super(LocationPO.class, 1, () -> null, repository);
    }

    @Override
    protected String getPoLabel(LocationPO po) {
        return po.getName();
    }

    @Override
    protected String getColumnLabelProperty() {
        return "name";
    }

}
