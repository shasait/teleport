package de.hasait.teleport.ui.puif;

import de.hasait.common.ui.puif.AbstractToOnePropertyUiFactory;
import de.hasait.teleport.domain.LocationPO;
import de.hasait.teleport.domain.LocationRepository;
import org.springframework.stereotype.Service;

@Service
public class LocationPuif extends AbstractToOnePropertyUiFactory<LocationPO, LocationRepository> {

    public LocationPuif(LocationRepository repository) {
        super(LocationPO.class, 1, repository);
    }

    @Override
    protected String getPoLabel(LocationPO locationPO) {
        return locationPO.getName();
    }

    @Override
    protected String getColumnLabelProperty() {
        return "name";
    }

}
