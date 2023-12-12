package de.hasait.teleport.ui.puif;

import de.hasait.common.ui.puif.AbstractToOnePuiFactory;
import de.hasait.teleport.domain.HypervisorPO;
import de.hasait.teleport.domain.HypervisorRepository;
import org.springframework.stereotype.Service;

@Service
public class HypervisorPuif extends AbstractToOnePuiFactory<HypervisorPO, HypervisorRepository, Void> {

    public HypervisorPuif(HypervisorRepository repository) {
        super(HypervisorPO.class, 1, () -> null, repository);
    }

    @Override
    protected String getPoLabel(HypervisorPO po) {
        return po.getName();
    }

    @Override
    protected String getColumnLabelProperty() {
        return "name";
    }

}
