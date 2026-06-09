import os
import shutil
import zipfile
from lxml import etree
from PIL import Image

SRC = r"D:\Working\Code\Android\MyAndroidPT\materials\实训3android与mysql的连接.docx"
OUT_DIR = r"D:\Working\Code\Android\MyAndroidPT\materials\images_3"

os.makedirs(OUT_DIR, exist_ok=True)

NS_W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
NS_A = 'http://schemas.openxmlformats.org/drawingml/2006/main'
NS_R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'
NS_PKG = 'http://schemas.openxmlformats.org/package/2006/relationships'

extract_dir = os.path.join(OUT_DIR, '_tmp_extract')
os.makedirs(extract_dir, exist_ok=True)
with zipfile.ZipFile(SRC, 'r') as z:
    z.extractall(extract_dir)

doc_xml_path = os.path.join(extract_dir, 'word', 'document.xml')
doc_tree = etree.parse(doc_xml_path)
doc_root = doc_tree.getroot()
body = doc_root.find(f'{{{NS_W}}}body')

paragraph_count = [0]
image_entries = []

def extract_drawings(elem, targets):
    tag = elem.tag.split('}')[-1] if '}' in elem.tag else elem.tag
    if tag == 'r':
        for d in elem.findall(f'{{{NS_W}}}drawing'):
            targets.append(d)
        for p in elem.findall(f'{{{NS_W}}}pict'):
            targets.append(p)
    elif tag == 'p':
        for r in elem.findall(f'{{{NS_W}}}r'):
            extract_drawings(r, targets)

for child in body:
    tag = child.tag.split('}')[-1] if '}' in child.tag else child.tag
    if tag in ('p',):
        paragraph_count[0] += 1
        drawings = []
        extract_drawings(child, drawings)
        for d in drawings:
            page = max(1, (paragraph_count[0] // 50) + 1)
            blip = d.find(f'.//{{{NS_A}}}blip')
            if blip is not None:
                rel_id = blip.get(f'{{{NS_R}}}embed')
                if rel_id:
                    image_entries.append((page, rel_id))
    elif tag == 'tbl':
        paragraph_count[0] += 1
        drawings = []
        for tc in child.findall(f'.//{{{NS_W}}}tc'):
            extract_drawings(tc, drawings)
        for d in drawings:
            page = max(1, (paragraph_count[0] // 50) + 1)
            blip = d.find(f'.//{{{NS_A}}}blip')
            if blip is not None:
                rel_id = blip.get(f'{{{NS_R}}}embed')
                if rel_id:
                    image_entries.append((page, rel_id))

print(f"Total inline shapes found: {len(image_entries)}")

image_rel_map = {}
for subdir in ['word', 'word/header', 'word/footer']:
    rels_dir = os.path.join(extract_dir, subdir, '_rels')
    if os.path.exists(rels_dir):
        for fname in os.listdir(rels_dir):
            if fname.endswith('.rels'):
                fpath = os.path.join(rels_dir, fname)
                tree = etree.parse(fpath)
                for rel in tree.getroot().findall(f'{{{NS_PKG}}}Relationship'):
                    rid = rel.get('Id')
                    target = rel.get('Target', '')
                    Type = rel.get('Type', '')
                    if 'image' in Type:
                        image_rel_map[rid] = target.lstrip('/')

print(f"Image rel map: {image_rel_map}")

seen = set()
unique = []
for page, rel_id in image_entries:
    if rel_id not in seen:
        seen.add(rel_id)
        unique.append((page, rel_id))

unique.sort(key=lambda x: x[0])

def get_size(filepath):
    try:
        img = Image.open(filepath)
        return img.size
    except:
        return None

results = []
for seq, (page, rel_id) in enumerate(unique, 1):
    rel_target = image_rel_map.get(rel_id, '')
    rel_filename = os.path.basename(rel_target)
    ext = os.path.splitext(rel_filename)[1].lower()
    out_name = f"img_{seq:02d}{ext}"
    src_path = os.path.join(extract_dir, rel_target.lstrip('/'))
    
    if not os.path.exists(src_path):
        src_path_alt = os.path.join(extract_dir, 'word', rel_target.lstrip('/'))
        if os.path.exists(src_path_alt):
            src_path = src_path_alt
        else:
            results.append((out_name, page, 'MISSING', None, None))
            continue
    
    if ext == '.emf':
        out_name = f"img_{seq:02d}.png"
        out_path = os.path.join(OUT_DIR, out_name)
        try:
            img = Image.open(open(src_path, 'rb'))
        except Exception:
            with open(src_path, 'wb') as f:
                f.write(open(src_path, 'rb').read())
            results.append((out_name, page, 'EMF(no converter)', None, None))
            continue
    else:
        out_path = os.path.join(OUT_DIR, out_name)
        shutil.copy2(src_path, out_path)
    
    size = get_size(out_path)
    results.append((out_name, page, ext.upper().lstrip('.'), size[0] if size else None, size[1] if size else None))

shutil.rmtree(extract_dir, ignore_errors=True)

print("\n=== Extracted Images ===")
for name, page, fmt, w, h in results:
    dim = f"{w}x{h}px" if w and h else "(无法获取尺寸)"
    print(f"  {name}  页码~{page}  {fmt}  {dim}")
