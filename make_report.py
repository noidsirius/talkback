import sys,os
from collections import defaultdict
tag = sys.argv[1]
result_dir=f"results/{tag}"
if os.path.isdir(result_dir):
    entries = []
    header = ["App","Test", "Result", "Steps", "Completed", "Failed", "Unlocatable", "Unreachable", "FirstProblem", "ReportedA11y", "TotalEvents", "TotalTime"]
    for report_path in os.listdir(result_dir):
        if not report_path.endswith('.txt'):
            continue
        path = result_dir +'/'+report_path
        with open(path, "r") as f:
            lines = f.readlines()
            app = report_path.split('.')[0].split('_')[0]
            test = "_".join(report_path.split('.')[0].split('_')[1:])
            entry = [app, test]
            if len(lines) > 0:
                line = lines[-1]
                for x in line.split('$'):
                    entry.append(x.split(':')[1].strip())
                entry[-1] = str(int(entry[-1])/1000)
                if entry[8] == "-1":
                    entry[8] = " "
            entries.append(entry)
    entries.sort(key=lambda x: x[0])
    print(",".join(header))
    for e in entries:
        print(",".join(e))
