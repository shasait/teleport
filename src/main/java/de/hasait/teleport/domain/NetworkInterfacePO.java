package de.hasait.teleport.domain;

import de.hasait.common.domain.IdAndVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "NETWORK_INTERFACE", uniqueConstraints = {
        @UniqueConstraint(name = "UC_NI_VM_NAME", columnNames = {"VM_ID", "NAME"})
})
public class NetworkInterfacePO implements IdAndVersion {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @ManyToOne
    @JoinColumn(name = "VM_ID", nullable = false)
    private VirtualMachinePO virtualMachine;

    @Size(min = 1, max = 16)
    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @Size(min = 1, max = 32)
    @NotNull
    @Column(name = "MODEL", nullable = false)
    private String model;

    @Size(max = 17)
    @Column(name = "MAC")
    private String mac;

    @Size(max = 15)
    @Column(name = "IP_4")
    private String ipv4;

    @Size(max = 39)
    @Column(name = "IP_6")
    private String ipv6;

    @ManyToOne
    @JoinColumn(name = "NETWORK_ID", nullable = false)
    private NetworkPO network;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    public VirtualMachinePO getVirtualMachine() {
        return virtualMachine;
    }

    public void setVirtualMachine(VirtualMachinePO virtualMachine) {
        this.virtualMachine = virtualMachine;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public String getIpv6() {
        return ipv6;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public NetworkPO getNetwork() {
        return network;
    }

    public void setNetwork(NetworkPO network) {
        this.network = network;
    }

}
