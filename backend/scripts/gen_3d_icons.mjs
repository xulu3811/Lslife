import fs from 'node:fs';
import path from 'node:path';
import sharp from 'sharp';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const outputDir = path.resolve(__dirname, '../public/assets/icons');

if (!fs.existsSync(outputDir)) {
  fs.mkdirSync(outputDir, { recursive: true });
}

const icons = [
  {
    name: '3d_flat_secondhand.png',
    title: '个人闲置',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFAD33"/>
          <stop offset="100%" stop-color="#E65100"/>
        </linearGradient>
        <linearGradient id="bag" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#FFE082"/>
          <stop offset="100%" stop-color="#FFB300"/>
        </linearGradient>
        <filter id="shadow" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <!-- Base Rounded Square -->
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <!-- 3D Bag -->
      <g filter="url(#shadow)">
        <path d="M 96 80 Q 96 52 128 52 Q 160 52 160 80" fill="none" stroke="#FFFFFF" stroke-width="12" stroke-linecap="round"/>
        <rect x="64" y="80" width="128" height="116" rx="20" ry="20" fill="url(#bag)"/>
        <path d="M 64 100 Q 128 116 192 100 L 192 80 A 20 20 0 0 0 172 80 L 84 80 A 20 20 0 0 0 64 80 Z" fill="rgba(255,255,255,0.3)"/>
        <!-- Tag -->
        <circle cx="128" cy="138" r="22" fill="#FFFFFF"/>
        <path d="M 118 138 L 126 146 L 140 130" fill="none" stroke="#4CAF50" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_housing.png',
    title: '房租租售',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg2" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#4DD0E1"/>
          <stop offset="100%" stop-color="#0288D1"/>
        </linearGradient>
        <linearGradient id="roof" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#1E88E5"/>
          <stop offset="100%" stop-color="#0D47A1"/>
        </linearGradient>
        <filter id="shadow2" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg2)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <g filter="url(#shadow2)">
        <rect x="160" y="60" width="20" height="32" rx="4" fill="#90A4AE"/>
        <path d="M 60 120 L 128 64 L 196 120 Z" fill="url(#roof)"/>
        <rect x="74" y="116" width="108" height="80" rx="8" fill="#FFFFFF"/>
        <rect x="114" y="150" width="28" height="46" rx="4" fill="#FF7043"/>
        <rect x="86" y="130" width="20" height="20" rx="4" fill="#FFEE58"/>
        <rect x="150" y="130" width="20" height="20" rx="4" fill="#FFEE58"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_cleaning.png',
    title: '家政保洁',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg3" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#4DB6AC"/>
          <stop offset="100%" stop-color="#00695C"/>
        </linearGradient>
        <linearGradient id="bottle" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stop-color="#FFFFFF"/>
          <stop offset="100%" stop-color="#E0F7FA"/>
        </linearGradient>
        <filter id="shadow3" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg3)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <g filter="url(#shadow3)">
        <path d="M 118 60 L 138 60 L 138 80 L 150 80 L 150 90 L 106 90 L 106 80 L 118 80 Z" fill="#00E5FF"/>
        <path d="M 106 70 L 86 76 L 86 86 L 106 80 Z" fill="#00838F"/>
        <rect x="96" y="90" width="64" height="106" rx="16" fill="url(#bottle)"/>
        <rect x="96" y="120" width="64" height="40" fill="#00897B"/>
        <!-- Sparkles -->
        <path d="M 180 70 Q 180 85 195 85 Q 180 85 180 100 Q 180 85 165 85 Q 180 85 180 70 Z" fill="#FFF59D"/>
        <path d="M 66 140 Q 66 150 76 150 Q 66 150 66 160 Q 66 150 56 150 Q 66 150 66 140 Z" fill="#FFFFFF"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_repair.png',
    title: '水电维修',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg4" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#9575CD"/>
          <stop offset="100%" stop-color="#4527A0"/>
        </linearGradient>
        <linearGradient id="wrench" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFFFFF"/>
          <stop offset="100%" stop-color="#90A4AE"/>
        </linearGradient>
        <filter id="shadow4" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg4)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <g filter="url(#shadow4)">
        <!-- Wrench -->
        <path d="M 70 166 L 150 86 A 28 28 0 1 1 182 118 L 102 198 A 20 20 0 1 1 70 166 Z" fill="url(#wrench)"/>
        <circle cx="166" cy="102" r="12" fill="#4527A0"/>
        <!-- Bolt -->
        <path d="M 120 54 L 70 130 L 110 130 L 96 196 L 160 110 L 120 110 Z" fill="#FFEB3B" stroke="#F9A825" stroke-width="4" stroke-linejoin="round"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_produce.png',
    title: '水果蔬菜',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg5" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#9CCC65"/>
          <stop offset="100%" stop-color="#2E7D32"/>
        </linearGradient>
        <linearGradient id="basket" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#8D6E63"/>
          <stop offset="100%" stop-color="#4E342E"/>
        </linearGradient>
        <filter id="shadow5" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg5)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <g filter="url(#shadow5)">
        <!-- Veggies & Apple behind basket -->
        <circle cx="106" cy="116" r="28" fill="#E53935"/>
        <path d="M 106 88 Q 112 80 116 82" fill="none" stroke="#5D4037" stroke-width="4" stroke-linecap="round"/>
        <!-- Leaves -->
        <path d="M 144 116 Q 130 80 166 70 Q 186 96 144 116 Z" fill="#66BB6A"/>
        <path d="M 160 116 Q 180 80 196 90 Q 186 116 160 116 Z" fill="#43A047"/>
        <!-- Basket -->
        <path d="M 60 120 L 196 120 L 180 190 Q 176 196 166 196 L 90 196 Q 80 196 76 190 Z" fill="url(#basket)"/>
        <rect x="54" y="112" width="148" height="14" rx="6" fill="#6D4C41"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_jobs.png',
    title: '招聘求职',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg6" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#F48FB1"/>
          <stop offset="100%" stop-color="#C2185B"/>
        </linearGradient>
        <linearGradient id="case" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#8D6E63"/>
          <stop offset="100%" stop-color="#4E342E"/>
        </linearGradient>
        <filter id="shadow6" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg6)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <g filter="url(#shadow6)">
        <path d="M 104 84 L 104 68 Q 104 58 114 58 L 142 58 Q 152 58 152 68 L 152 84" fill="none" stroke="#FFCA28" stroke-width="8" stroke-linecap="round"/>
        <rect x="56" y="84" width="144" height="104" rx="16" fill="url(#case)"/>
        <rect x="56" y="124" width="144" height="12" fill="#3E2723"/>
        <rect x="116" y="118" width="24" height="24" rx="4" fill="#FFCA28"/>
        <!-- Badge/Tie -->
        <circle cx="170" cy="156" r="22" fill="#42A5F5"/>
        <path d="M 160 156 L 168 164 L 182 148" fill="none" stroke="#FFFFFF" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_car_rental.png',
    title: '租车服务',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg7" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFCA28"/>
          <stop offset="100%" stop-color="#E65100"/>
        </linearGradient>
        <linearGradient id="car" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stop-color="#E3F2FD"/>
          <stop offset="100%" stop-color="#90CAF9"/>
        </linearGradient>
        <filter id="shadow7" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg7)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <g filter="url(#shadow7)">
        <path d="M 60 140 L 76 96 Q 82 82 96 82 L 164 82 Q 178 82 186 96 L 196 140 L 204 140 Q 212 140 212 150 L 212 170 Q 212 176 206 176 L 50 176 Q 44 176 44 170 L 44 150 Q 44 140 52 140 Z" fill="url(#car)"/>
        <!-- Windows -->
        <path d="M 82 98 L 124 98 L 124 136 L 68 136 Z" fill="#1E88E5"/>
        <path d="M 132 98 L 174 98 L 188 136 L 132 136 Z" fill="#1565C0"/>
        <!-- Wheels -->
        <circle cx="80" cy="176" r="20" fill="#37474F"/>
        <circle cx="80" cy="176" r="8" fill="#CFD8DC"/>
        <circle cx="176" cy="176" r="20" fill="#37474F"/>
        <circle cx="176" cy="176" r="8" fill="#CFD8DC"/>
      </g>
    </svg>`
  },
  {
    name: '3d_flat_parttime.png',
    title: '兼职零工',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 256 256" width="256" height="256">
      <defs>
        <linearGradient id="bg8" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#BA68C8"/>
          <stop offset="100%" stop-color="#4A148C"/>
        </linearGradient>
        <linearGradient id="clock" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#FFF59D"/>
          <stop offset="100%" stop-color="#FBC02D"/>
        </linearGradient>
        <filter id="shadow8" x="-10%" y="-10%" width="120%" height="120%">
          <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.3"/>
        </filter>
      </defs>
      <rect x="12" y="16" width="232" height="232" rx="56" ry="56" fill="rgba(0,0,0,0.2)"/>
      <rect x="12" y="12" width="232" height="232" rx="56" ry="56" fill="url(#bg8)"/>
      <path d="M 68 12 Q 128 20 188 12 A 56 56 0 0 1 244 68 L 244 100 Q 128 120 12 100 L 12 68 A 56 56 0 0 1 68 12 Z" fill="rgba(255,255,255,0.25)"/>
      
      <g filter="url(#shadow8)">
        <!-- Bells -->
        <circle cx="82" cy="74" r="16" fill="#F57F17"/>
        <circle cx="174" cy="74" r="16" fill="#F57F17"/>
        <path d="M 110 52 L 146 52" stroke="#F57F17" stroke-width="8" stroke-linecap="round"/>
        <!-- Clock Body -->
        <circle cx="128" cy="128" r="56" fill="url(#clock)"/>
        <circle cx="128" cy="128" r="44" fill="#FFFFFF"/>
        <!-- Coin/Money Symbol -->
        <circle cx="128" cy="128" r="28" fill="#4CAF50"/>
        <text x="128" y="138" font-size="32" font-weight="bold" fill="#FFFFFF" text-anchor="middle" font-family="sans-serif">¥</text>
        <!-- Stars -->
        <path d="M 60 110 L 63 118 L 71 119 L 65 125 L 67 133 L 60 129 L 53 133 L 55 125 L 49 119 L 57 118 Z" fill="#FFEB3B"/>
        <path d="M 196 150 L 198 156 L 204 157 L 199 161 L 200 167 L 196 164 L 192 167 L 193 161 L 188 157 L 194 156 Z" fill="#FFEB3B"/>
      </g>
    </svg>`
  }
];

async function generate() {
  console.log('Generating 3D flat PNG icons...');
  for (const item of icons) {
    const filePath = path.join(outputDir, item.name);
    const buffer = Buffer.from(item.svg);
    await sharp(buffer).png().toFile(filePath);
    console.log(`Generated: ${item.name} (${item.title}) -> ${filePath}`);
  }
  console.log('All 8 icons generated successfully.');
}

generate().catch(err => {
  console.error(err);
  process.exit(1);
});
