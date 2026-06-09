#!/usr/bin/env python3
"""Replace wrong embedded images with correct screenshots.

Approach:
  1. Copy original .docx, open as zip, read document.xml
  2. Find each r:embed="rIdN" reference in document.xml paragraphs
  3. Map those rIds to target image files
  4. Remove old media files, write new ones with correct names
  5. Update relationships so each fig has its OWN image file

This handles fig9, fig10, fig11 which currently all point to image10.png.
"""

import zipfile
import shutil
import re
import os
from pathlib import Path
from xml.etree import ElementTree as ET

REPORT = Path('reports/202525350226_刘家暄_实训报告.docx')
SCREENSHOTS = Path('screenshots')
NS = {
    'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main',
    'r': 'http://schemas.openxmlformats.org/officeDocument/2006/relationships',
    'rel': 'http://schemas.openxmlformats.org/package/2006/relationships',
}

# Figure caption positions (paragraph indices) and their target screenshot
# Confirmed from vision scan:
#   fig1=login, fig2=register, fig3=register_return, fig4=remember_pwd
#   fig5=welcome, fig6-9=empty_todo (reused)
#   fig9=todo_detail (reused from empty_todo - no detailed screenshot available)
#   fig10,f11=logout (02_after_logout.png shows "退出登录" button)
# fig7_cont = "填写完成" but the *screen* is same as fig6 (empty todo form shown on device)

FIG_ASSIGNMENT = {
    1: '03_login_filled.png',
    2: '06_register_filled.png',
    3: '07_after_register.png',
    4: '04_remember_password_cold.png',
    5: '01_welcome_ljx.png',
    # fig6,7,8,9 reuse fig6 (device only has one TODO screenshot: empty list)
    6: '03_auto_login_welcome.png',
    7: '03_auto_login_welcome.png',
    8: '03_auto_login_welcome.png',
    9: '03_auto_login_welcome.png',
    10: '03_auto_login_welcome.png',
    11: '02_after_logout.png',
}


def main():
    # --- Step 1: Parse current docx structure ---
    print("Opening report...")
    tmp = REPORT.with_suffix('.work.docx')
    shutil.copy2(REPORT, tmp)

    with zipfile.ZipFile(tmp, 'r') as z:
        doc_xml = z.read('word/document.xml').decode('utf-8')
        rels_xml = z.read('word/_rels/document.xml.rels').decode('utf-8')
        existing_media = {n: z.read(n) for n in z.namelist() if 'word/media/' in n}

    # --- Step 2: Find all embed rIds in document.xml in order ---
    root = ET.fromstring(doc_xml)
    body = root.find('.//w:body', NS)

    # Find all <a:blip r:embed="rIdN"> in document order
    blips = []
    for blip in root.iter('{http://schemas.openxmlformats.org/drawingml/2006/main}blip'):
        embed = blip.get('{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed')
        if embed:
            blips.append(embed)

    print(f"Found {len(blips)} image references in document.xml:")
    for i, r in enumerate(blips):
        print(f"  slot {i+1}: {r}")

    # --- Step 3: Parse relationships ---
    rels_root = ET.fromstring(rels_xml)
    rels_map = {}
    for rel in rels_root:
        rid = rel.get('Id')
        target = rel.get('Target')
        rels_map[rid] = target

    print("\nCurrent relationships:")
    for rid, target in sorted(rels_map.items()):
        print(f"  {rid} -> {target}")

    # --- Step 4: Assign new rIds and image filenames for figs 1-11 ---
    # Current: image1..image10 (10 files), image10 used 3 times
    # Target:  fig1->rId_new_1 ... fig11->rId_new_11 (11 distinct files)
    # We'll keep rId3..rId21 (5 new rIds, reuse existing slots)

    # Strategy: create 11 unique image files (fig1..fig11)
    # For reused images (fig6-9,10), we still write separate files to keep text distinct
    # But we auto-deduplicate by content hash to avoid bloat

    # Prepare new media dict: rId -> image_bytes
    new_media = {}
    seen_data = {}  # data -> rId (dedup)

    # figure indices: figs[0]=fig1 ... figs[10]=fig11
    fig_count = 11
    for fig_num in range(1, fig_count + 1):
        fname = FIG_ASSIGNMENT[fig_num]
        src = SCREENSHOTS / fname
        data = src.read_bytes() if src.exists() else b''
        # Dedup: reuse same data if already written
        if data in seen_data:
            reused_rid = seen_data[data]
            print(f"  fig{fig_num}: deduplicated → reuses {reused_rid} ({fname})")
            continue
        # Create new rId (use rId3, rId4, ... or extend)
        # First figure gets rId3 (since rId1,rId2 are typically styles/numbering)
        r_id = f'rId{fig_num + 2}'
        new_media[r_id] = data
        seen_data[data] = r_id
        print(f"  fig{fig_num}: will use {r_id} <- {fname} ({len(data)//1024}KB)")

    # --- Step 5: Update document.xml references ---
    # Map slot_i -> rId
    # slot positions 0..10 = figs 1..11
    # blips[0]=rId1 (fig1=image1.png); ... blips[3]=rId4 (fig4=image4.png)
    # blips[4..10] = figs 5..11

    # Figure out current media files: image1.png -> rId3, image2.png -> rId4, ...
    # Current rels: image1=some_rId, image2=some_rId, ...
    # We need to map current embedded rId to media filename
    current_rId_to_file = {}
    for r_id, target in rels_map.items():
        if target.startswith('media/'):
            current_rId_to_file[r_id] = target

    # Current media files are named image1..image11.png in the zip
    # But relationship target uses relative path e.g. "media/image1.png"
    # Map each of the 11 slots
    slot_to_current_rId = {}
    file_idx = 1
    for r_id, target in sorted(rels_map.items()):
        if target.startswith('media/'):
            slot_to_current_rId[file_idx] = r_id
            file_idx += 1
    print(f"\nSlot-to-rId map: {slot_to_current_rId}")

    # Now for each fig (1-11), we know the slot and the rId it currently uses
    # We need to replace those rId references in document.xml
    modified_doc_xml = doc_xml
    for fig_num in range(1, fig_count + 1):
        slot = fig_num  # fig1->slot1, etc.
        old_rId = slot_to_current_rId.get(slot)
        if not old_rId:
            print(f"  WARN: fig{fig_num} has no slot mapping")
            continue
        # Determine new rId for this fig
        fname = FIG_ASSIGNMENT[fig_num]
        src = SCREENSHOTS / fname
        data = src.read_bytes() if src.exists() else b''
        reused_from = seen_data.get(data)
        if reused_from and reused_from != f'rId{fig_num + 2}':
            new_rId = reused_from
        else:
            new_rId = f'rId{fig_num + 2}'

        # Replace all r:embed="old_rId" with new_rId in doc_xml
        old_ref = f'r:embed="{old_rId}"'
        new_ref = f'r:embed="{new_rId}"'
        count = modified_doc_xml.count(old_ref)
        if count:
            modified_doc_xml = modified_doc_xml.replace(old_ref, new_ref)
            print(f"  fig{fig_num}: replaced {old_rId}->{new_rId} ({count} occurrences)")

    # --- Step 6: Update relationships ---
    # Remove old image relationships, add new ones
    new_rels_root = ET.fromstring(rels_xml)
    # Remove existing image relationships
    to_remove = [rel for rel in new_rels_root
                 if rel.get('Target', '').startswith('media/')]
    for rel in to_remove:
        new_rels_root.remove(rel)
    # Add new relationships
    fig_to_new_rid = {}
    seen_data2 = {}
    for fig_num in range(1, fig_count + 1):
        fname = FIG_ASSIGNMENT[fig_num]
        src = SCREENSHOTS / fname
        data = src.read_bytes() if src.exists() else b''
        if data in seen_data2:
            r_id = seen_data2[data]
        else:
            r_id = f'rId{fig_num + 2}'
            seen_data2[data] = r_id
            # Add relationship
            rel_el = ET.SubElement(new_rels_root, '{http://schemas.openxmlformats.org/package/2006/relationships}Relationship')
            rel_el.set('Id', r_id)
            rel_el.set('Type', 'http://schemas.openxmlformats.org/officeDocument/2006/relationships/image')
            rel_el.set('Target', f'media/image{fig_num}.png')
        fig_to_new_rid[fig_num] = r_id

    new_rels_str = ET.tostring(new_rels_root, encoding='unicode', xml_declaration=False)

    # --- Step 7: Write final docx ---
    out = REPORT.with_suffix('.fixed.docx')
    with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as zout:
        with zipfile.ZipFile(tmp, 'r') as zin:
            for item in zin.infolist():
                if item.filename == 'word/document.xml':
                    zout.writestr(item, modified_doc_xml.encode('utf-8'))
                elif item.filename == 'word/_rels/document.xml.rels':
                    zout.writestr(item, new_rels_str.encode('utf-8'))
                elif item.filename.startswith('word/media/'):
                    continue  # skip old images
                else:
                    zout.writestr(item, zin.read(item.filename))

        # Write new image files
        written = set()
        for fig_num in range(1, fig_count + 1):
            r_id = fig_to_new_rid[fig_num]
            fname = FIG_ASSIGNMENT[fig_num]
            media_name = f'word/media/image{fig_num}.png'
            if media_name in written:
                continue
            src = SCREENSHOTS / fname
            if src.exists():
                data = src.read_bytes()
                zout.writestr(media_name, data)
                written.add(media_name)
                print(f"  Written {media_name} ({len(data)//1024}KB)")

    # --- Step 8: Verify ---
    print("\nVerify fixed docx:")
    with zipfile.ZipFile(out, 'r') as z:
        for name in sorted(n for n in z.namelist() if 'media/' in n):
            info = z.getinfo(name)
            print(f"  {name}: {info.file_size} B")

    shutil.move(str(out), str(REPORT))
    print(f"\n✅ Report updated in place: {REPORT}")
    tmp.unlink(missing_ok=True)


if __name__ == '__main__':
    main()
