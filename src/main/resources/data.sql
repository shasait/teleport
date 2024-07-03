insert into LOCATION (
    ID, VERSION,
    NAME
) values (
    NEXT VALUE FOR LOCATION_SEQ, 1,
    'test-location'
);

insert into HOST (
    ID, VERSION,
    NAME,
    LOCATION_ID
) values (
    NEXT VALUE FOR HOST_SEQ, 1,
    'hv1',
    select ID from LOCATION where NAME = 'test-location'
);

insert into STORAGE (
    ID, VERSION,
    NAME,
    HOST_ID,
    DRIVER,
    DRIVER_CONFIG,
    AVAIL_BYTES
) values (
    NEXT VALUE FOR STORAGE_SEQ, 1,
    'fast',
    select ID from HOST where NAME = 'hv1',
    'zfs',
    '{ ''dataset'':''rpool/srv/vms'' }',
    0
);

insert into HYPERVISOR (
    ID, VERSION,
    NAME,
    HOST_ID,
    DRIVER,
    DRIVER_CONFIG
) values (
    NEXT VALUE FOR HYPERVISOR_SEQ, 1,
    'default',
    select ID from HOST where NAME = 'hv1',
    'virsh',
    '{}'
);
