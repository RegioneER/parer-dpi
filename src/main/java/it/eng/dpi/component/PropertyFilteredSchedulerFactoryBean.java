/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package it.eng.dpi.component;

import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

public class PropertyFilteredSchedulerFactoryBean extends SchedulerFactoryBean {

    private final static String DAILY_SYNC_JOB = "cronTriggerDailySyncJob";
    private final static String WEEKLY_SYNC_JOB = "cronTriggerWeeklySyncJob";
    private final static String MONTHLY_SYNC_JOB = "cronTriggerMonthlySyncJob";

    @Autowired
    private DPIContext context;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Scheduler sched = this.getScheduler();
        for (TriggerKey key : sched.getTriggerKeys(GroupMatcher.triggerGroupEquals(TriggerKey.DEFAULT_GROUP))) {
            if (key.getName().equals(DAILY_SYNC_JOB) && !context.isDailyEnable()) {
                sched.pauseTrigger(key);
                continue;
            }
            if (key.getName().equals(WEEKLY_SYNC_JOB) && !context.isWeeklyEnable()) {
                sched.pauseTrigger(key);
                continue;
            }
            if (key.getName().equals(MONTHLY_SYNC_JOB) && !context.isMonthlyEnable()) {
                sched.pauseTrigger(key);
                continue;
            }
        }

    }

}
