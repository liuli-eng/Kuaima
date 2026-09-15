const sharp = require('sharp');
const path = require('path');
const fs = require('fs');

async function getImageInfo(filePath) {
  const metadata = await sharp(filePath).metadata();
  const stats = fs.statSync(filePath);
  return {
    width: metadata.width,
    height: metadata.height,
    sizeKB: Math.round(stats.size / 1024 * 10) / 10
  };
}

async function resizeImage(inputPath, maxSize) {
  const tempPath = inputPath + '.tmp';
  await sharp(inputPath)
    .resize(maxSize, maxSize, { fit: 'inside', withoutEnlargement: true })
    .png({ quality: 80, compressionLevel: 9 })
    .toFile(tempPath);
  fs.renameSync(tempPath, inputPath);
}

async function main() {
  const avatarsDir = path.join(__dirname, 'static', 'avatars');

  const bossAvatar = path.join(avatarsDir, 'default-boss-avatar.png');
  const workerAvatar = path.join(avatarsDir, 'default-worker-avatar.png');

  console.log('=== Original Image Info ===');
  const bossInfo = await getImageInfo(bossAvatar);
  console.log(`Boss avatar: ${bossInfo.width}x${bossInfo.height}, ${bossInfo.sizeKB}KB`);

  const workerInfo = await getImageInfo(workerAvatar);
  console.log(`Worker avatar: ${workerInfo.width}x${workerInfo.height}, ${workerInfo.sizeKB}KB`);

  console.log('\n=== Resizing to 200x200 ===');
  await resizeImage(bossAvatar, 200);
  await resizeImage(workerAvatar, 200);

  console.log('\n=== Compressed Image Info ===');
  const newBossInfo = await getImageInfo(bossAvatar);
  console.log(`Boss avatar: ${newBossInfo.width}x${newBossInfo.height}, ${newBossInfo.sizeKB}KB`);

  const newWorkerInfo = await getImageInfo(workerAvatar);
  console.log(`Worker avatar: ${newWorkerInfo.width}x${newWorkerInfo.height}, ${newWorkerInfo.sizeKB}KB`);

  const totalOriginal = bossInfo.sizeKB + workerInfo.sizeKB;
  const totalNew = newBossInfo.sizeKB + newWorkerInfo.sizeKB;
  console.log(`\nTotal: ${totalOriginal}KB -> ${totalNew}KB (saved ${Math.round((totalOriginal - totalNew) / totalOriginal * 100)}%)`);
}

main().catch(console.error);
