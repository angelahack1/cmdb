/*
 * CMDB Mongo bootstrap for mongosh
 *
 * What this script does:
 * 1) switches to the target database (default: cmdb)
 * 2) creates the Assets collection if needed
 * 3) creates indexes used by the demo
 * 4) inserts seed asset documents if they do not already exist
 * 5) prints a verification summary
 *
 * Usage examples:
 *   mongosh "mongodb://cmdbApp:changeme@localhost:27017/cmdb?authSource=cmdb" --file mongo/bootstrap-cmdb.js
 *   mongosh "mongodb://localhost:27017" --eval "var APP_DB='cmdb'; var APP_USER='cmdbApp'; var APP_PASSWORD='changeme'" --file mongo/bootstrap-cmdb.js
 *
 * Optional variables before running:
 *   APP_DB          -> database name to use (default: "cmdb")
 *   APP_USER        -> application user for the printed connection example (default: "cmdbApp")
 *   APP_PASSWORD    -> application password for the printed connection example (default: "changeme")
 *   AUTH_DB         -> authSource database for the printed connection example (default: APP_DB)
 *   DROP_FIRST      -> true to drop the target database first (default: false)
 *   RESEED_ASSETS   -> true to wipe only the Assets collection before reseeding (default: false)
 */

(function () {
  const dbName = typeof APP_DB !== 'undefined' && APP_DB ? APP_DB : 'cmdb';
  const appUser = typeof APP_USER !== 'undefined' && APP_USER ? APP_USER : 'cmdbApp';
  const appPassword = typeof APP_PASSWORD !== 'undefined' && APP_PASSWORD ? APP_PASSWORD : 'changeme';
  const authDb = typeof AUTH_DB !== 'undefined' && AUTH_DB ? AUTH_DB : dbName;
  const dropFirst = typeof DROP_FIRST !== 'undefined' ? !!DROP_FIRST : false;
  const reseedAssets = typeof RESEED_ASSETS !== 'undefined' ? !!RESEED_ASSETS : false;
  const collectionName = 'Assets';
  const appUri = 'mongodb://' + encodeURIComponent(appUser) + ':' + encodeURIComponent(appPassword)
    + '@localhost:27017/' + dbName + '?authSource=' + encodeURIComponent(authDb);

  const targetDb = db.getSiblingDB(dbName);

  print('============================================================');
  print('CMDB Mongo bootstrap');
  print('Database      : ' + dbName);
  print('Collection    : ' + collectionName);
  print('DROP_FIRST    : ' + dropFirst);
  print('RESEED_ASSETS : ' + reseedAssets);
  print('============================================================');

  if (dropFirst) {
    const dropResult = targetDb.dropDatabase();
    print('dropDatabase  : ' + tojson(dropResult));
  }

  const collectionExists = targetDb.getCollectionInfos({ name: collectionName }).length > 0;
  if (!collectionExists) {
    const createResult = targetDb.createCollection(collectionName);
    print('createCollection: ' + tojson(createResult));
  } else {
    print('createCollection: skipped (already exists)');
  }

  const assets = targetDb.getCollection(collectionName);

  if (reseedAssets) {
    const deleteResult = assets.deleteMany({});
    print('deleteMany({}) : deleted=' + deleteResult.deletedCount);
  }

  const indexResults = [];
  indexResults.push(assets.createIndex({ createdAt: -1 }, { name: 'idx_assets_createdAt_desc' }));
  indexResults.push(assets.createIndex({ name: 1 }, { name: 'idx_assets_name_asc' }));
  indexResults.push(assets.createIndex({ owner: 1, type: 1 }, { name: 'idx_assets_owner_type_asc' }));

  print('indexes:');
  indexResults.forEach(function (idxName) {
    print('  - ' + idxName);
  });

  const seedAssets = [
    {
      name: 'Core Router MX-01',
      type: 'Network',
      owner: 'Infrastructure',
      createdAt: ISODate('2026-03-01T08:00:00Z')
    },
    {
      name: 'Payroll API',
      type: 'Application',
      owner: 'Finance IT',
      createdAt: ISODate('2026-03-02T09:15:00Z')
    },
    {
      name: 'Oracle PROD Cluster',
      type: 'Database',
      owner: 'DBA Team',
      createdAt: ISODate('2026-03-03T10:30:00Z')
    },
    {
      name: 'Windows File Server FS-02',
      type: 'Server',
      owner: 'Operations',
      createdAt: ISODate('2026-03-04T11:45:00Z')
    },
    {
      name: 'CyberArk Vault',
      type: 'Security',
      owner: 'Cybersecurity',
      createdAt: ISODate('2026-03-05T13:00:00Z')
    },
    {
      name: 'Service Desk Portal',
      type: 'Application',
      owner: 'ITSM',
      createdAt: ISODate('2026-03-06T14:15:00Z')
    },
    {
      name: 'GitLab Runner 01',
      type: 'DevOps',
      owner: 'Platform Engineering',
      createdAt: ISODate('2026-03-07T15:30:00Z')
    },
    {
      name: 'Backup Repository',
      type: 'Storage',
      owner: 'Infrastructure',
      createdAt: ISODate('2026-03-08T16:45:00Z')
    }
  ];

  let inserted = 0;
  let skipped = 0;

  seedAssets.forEach(function (asset) {
    const existing = assets.findOne({ name: asset.name, owner: asset.owner });
    if (existing) {
      skipped += 1;
      return;
    }
    assets.insertOne(asset);
    inserted += 1;
  });

  print('seed results: inserted=' + inserted + ', skipped=' + skipped);
  print('document count: ' + assets.countDocuments({}));

  print('latest assets preview:');
  assets.find({}, { _id: 1, name: 1, type: 1, owner: 1, createdAt: 1 })
    .sort({ createdAt: -1 })
    .limit(10)
    .forEach(function (doc) {
      print('  ' + tojson(doc));
    });

  print('');
  print('App configuration to match this bootstrap:');
  print('  MONGODB_URI=' + appUri);
  print('  MONGODB_DB=' + dbName);
  print('  MONGODB_USER=' + appUser);
  print('  MONGODB_PASSWORD=' + appPassword);
  print('');
  print('Done.');
})();