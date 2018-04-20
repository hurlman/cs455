using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using CsvHelper;
using MathNet.Numerics.Statistics;

namespace CorelationCalculator
{
    class Program
    {
        static void Main(string[] args)
        {
            var empPath = Environment.CurrentDirectory + "\\employment\\total_in_selective_area\\part-00000";
            var housePath = Environment.CurrentDirectory + "\\housing\\yearlyTotals\\part-00000";
            var popPath = Environment.CurrentDirectory + "\\population\\yearlyTotals\\part-00000";

            var empLines = File.ReadAllLines(empPath);
            var houseLines = File.ReadAllLines(housePath);
            var popLines = File.ReadAllLines(popPath);

            var numOnly = new Regex("[^0-9.,]");
            var empLinesScrubbed = empLines.Select(line => numOnly.Replace(line, ""));
            var houseLinesScrubbed = houseLines.Select(line => numOnly.Replace(line, ""));
            var popLinesScrubbed = popLines.Select(line => numOnly.Replace(line, ""));

            var metros = new Dictionary<string, string>();
            var metrosPath = Environment.CurrentDirectory + "\\CountyCrossWalk_Zillow.csv";
            var textReader = File.OpenText(metrosPath);
            var csv = new CsvReader(textReader);
            while (csv.Read())
            {
                if (!metros.ContainsKey(csv.GetField(9)))
                metros.Add(csv.GetField(9), csv.GetField(5));
            }

            var houseData = BuildData(houseLinesScrubbed);
            var popData = BuildData(popLinesScrubbed);
            var empData = new Dictionary<string, double[]>();
            foreach (var empLine in empLinesScrubbed)
            {
                var data = empLine.Split(',');
                var key = data[0];
                var value = new List<double>();
                for (var i = 2; i < data.Length - 2; i += 2)
                {
                    value.Add(Convert.ToDouble(data[i]));
                }
                empData.Add(key, value.ToArray());
            }

            var commonMetros = new HashSet<string>(empData.Keys);
            commonMetros.IntersectWith(popData.Keys);
            commonMetros.IntersectWith(houseData.Keys);

            var corOut = "Totals_Correlation_Output.csv";
            using (var sw = File.AppendText(corOut))
            {
                sw.WriteLine("ID,Name,Housing_Populaiton,Housing_Employment,Employment_Population");
                foreach (var metro in commonMetros)
                {
                    var hpCorr = Correlation.Pearson(houseData[metro], popData[metro]);
                    var heCorr = Correlation.Pearson(houseData[metro], empData[metro]);
                    var epCorr = Correlation.Pearson(empData[metro], popData[metro]);

                    sw.WriteLine($"{metro},\"{metros[metro]}\",{hpCorr},{heCorr},{epCorr}");
                }
            }
        }

        static Dictionary<string, double[]> BuildData(IEnumerable<string> dataIn)
        {
            var outData = new Dictionary<string, double[]>();
            foreach (var line in dataIn)
            {
                var data = line.Split(',');
                var key = data[1];
                var value = new List<double>();
                for (var i = 2; i < data.Length; i++)
                {
                    value.Add(Convert.ToDouble(data[i]));
                }
                outData.Add(key, value.ToArray());
            }

            return outData;
        }
    }
}
