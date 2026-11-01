import json
import pathlib
import shutil
import subprocess

root = pathlib.Path(__file__).resolve().parent
driver = root / 'driver'
results = []
for mode in ['clean', 'workflow', 'first', 'middle', 'last', 'only', 'invalid']:
    folder = root / 'runs-verified' / mode
    folder.mkdir(parents=True, exist_ok=False)
    shutil.copy2(root / 'candidate-osps.jar', folder / 'osps.jar')
    if mode != 'clean':
        shutil.copytree(root / 'fixture-v13' / 'data', folder / 'data')
    before = (folder / 'data' / 'addressbook.json')
    if before.exists():
        shutil.copy2(before, folder / 'baseline.json')
    cmd = ['java', '--enable-native-access=ALL-UNNAMED', '-cp',
           str(driver) + ';osps.jar', 'CandidateSmoke', mode]
    with (folder / ('console-' + mode + '.txt')).open('w', encoding='utf-8') as out:
        result = subprocess.run(cmd, cwd=folder, stdout=out, stderr=subprocess.STDOUT, timeout=90)
    results.append({'mode': mode, 'exitCode': result.returncode})
    print(mode, result.returncode, flush=True)
    if result.returncode:
        print((folder / ('result-' + mode + '.txt')).read_text(), flush=True)
        break
    if mode == 'workflow':
        shutil.copy2(folder / 'data' / 'addressbook.json', folder / 'before-restart.json')
        with (folder / 'console-restart.txt').open('w', encoding='utf-8') as out:
            result = subprocess.run(cmd[:-1] + ['restart'], cwd=folder, stdout=out,
                                    stderr=subprocess.STDOUT, timeout=90)
        results.append({'mode': 'restart', 'exitCode': result.returncode})
        print('restart', result.returncode, flush=True)
        actual = json.loads((folder / 'data' / 'addressbook.json').read_text())
        baseline = json.loads((folder / 'baseline.json').read_text())
        assert actual['persons'] == baseline['persons'], 'surviving JSON records changed after restart'
        assert actual['nextDebtorId'] == baseline['nextDebtorId'] + 1, 'next ID reused'
    if mode == 'invalid':
        assert before.read_bytes() == (folder / 'baseline.json').read_bytes(), 'invalid input changed stored JSON'
(root / 'run-results.json').write_text(json.dumps(results, indent=2))
assert len(results) == 8 and all(r['exitCode'] == 0 for r in results)
