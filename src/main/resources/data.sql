insert into LOCATION (
    ID, VERSION,
    NAME
) values (
    NEXT VALUE FOR LOCATION_SEQ, 1,
    'test-location'
);

insert into HYPERVISOR (
    ID, VERSION,
    NAME,
    LOCATION_ID
) values (
    NEXT VALUE FOR HYPERVISOR_SEQ, 1,
    'hv1',
    select ID from LOCATION where NAME = 'test-location'
);

insert into STORAGE (
    ID, VERSION,
    NAME,
    HYPERVISOR_ID,
    DRIVER,
    DRIVER_CONFIG,
    AVAIL_BYTES
) values (
    NEXT VALUE FOR STORAGE_SEQ, 1,
    'fast',
    select ID from HYPERVISOR where NAME = 'hv1',
    'zfs',
    '{ ''dataset'':''rpool/srv/vms'' }',
    0
);
