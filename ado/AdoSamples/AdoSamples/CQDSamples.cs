using EsgynDB.Data;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AdoSamples
{
    [TestClass]
    public class CQDSamples
    {
        private log4net.ILog log = log4net.LogManager.GetLogger(typeof(CQDSamples));
        private string ConnectionString = "server=10.10.10.136:23400;user=zz;password=zz;schema=ado";

        [TestMethod]
        public void setCQDs()
        {

                using (EsgynDBConnection conn = new EsgynDBConnection())
                {
                    conn.ConnectionString = ConnectionString;
                    conn.Open();
                    EsgynDBCommand cmd = conn.CreateCommand();
                    log.Info("ESP_INACTIVE_TIMEOUT '66666'");
                    cmd.CommandText = "SET SESSION DEFAULT ESP_INACTIVE_TIMEOUT '666666'";
                    cmd.ExecuteNonQuery();
                    log.Info("ESP_IDLE_TIMEOUT '77777'");
                    cmd.CommandText = "SET SESSION DEFAULT ESP_IDLE_TIMEOUT '666666'";
                    cmd.ExecuteNonQuery();
                    log.Info("SHOWCONTROL_SHOW_ALL 'on'!");
                    cmd.CommandText = "CONTROL QUERY DEFAULT SHOWCONTROL_SHOW_ALL 'on'";
                    cmd.ExecuteNonQuery();

                    log.Info("SHOWCONTROL ALL!");
                    cmd.CommandText = "SHOWCONTROL ALL";
                    EsgynDBDataReader ca = cmd.ExecuteReader();
                    bool flag = false;
                    while (ca.Read())
                    {
                        if(!flag)
                            flag = true;
                        log.Info(ca.GetValue(0) +"");
                    }
                    log.Info("Done!");

                    /*
                     * 
                     *Continue to do whatever you want...
                     *
                     */

                    Assert.IsTrue(flag, "No data in query 'SHOWCONTROL ALL'");
            }


        }
    }
}
