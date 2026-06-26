import docx

doc = docx.Document('BAO_CAO_DO_AN_CS1_TAI.docx')
with open('c:\\Users\\HunterZ\\Desktop\\FINAL EXAM JAVA\\docx_text.txt', 'w', encoding='utf-8') as f:
    for p in doc.paragraphs:
        if p.text.strip():
            f.write(p.text + '\n')
